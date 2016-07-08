/*
 *
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.symphonyoss.webroomdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.webroomdesk.config.HelpBotConfig;
import org.symphonyoss.webroomdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webroomdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.webroomdesk.listeners.presence.MemberPresenceListener;
import org.symphonyoss.webroomdesk.models.HelpBotSession;
import org.symphonyoss.webroomdesk.models.users.Member;
import org.symphonyoss.webroomdesk.utils.ClientCache;
import org.symphonyoss.webroomdesk.utils.HoldCache;
import org.symphonyoss.webroomdesk.utils.MemberCache;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.User;

import static org.symphonyoss.webroomdesk.config.HelpBotConfig.Config;

import java.util.Set;

/**
 * The main help desk bot class
 * REQUIRED VM Arguments or System Properties:
 *
 *        -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 *        -Dkeyauth.url=https://pod_fqdn:port/keyauth
 *        -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 *        -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 *        -Dcerts.dir=/dev/certs/
 *        -Dkeystore.password=(Pass)
 *        -Dtruststore.file=/dev/certs/server.truststore
 *        -Dtruststore.password=(Pass)
 *        -Dbot.user=hashtag.bot
 *        -Dbot.domain=@markit.com
 *
 *
 */
public class HelpDeskBot implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(HelpDeskBot.class);
    private MemberCommandListener memberCommandListener;
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;



    public HelpDeskBot() {
        logger.info("Init for help desk user {}", Config.getString(HelpBotConfig.BOT_USER));
        initConnection();
        setupBot();
    }

    public static void main(String[] args) {
        System.out.println("HelpDeskBot starting...");

        new HelpDeskBot();
    }

    /**
     * Sets up the bot.
     * Loads all the members from file into the cache.
     * Registers web service.
     * Instantiates web listeners.
     * Starts threads (inactivity).
     */
    public void setupBot() {
        try {

            MemberCache.loadMembers();

            addAdmin();

            HelpBotSession helpBotSession = new HelpBotSession();

            symClient.getChatService().registerListener(this);
            helpBotSession.setSymphonyClient(symClient);

            helpClientListener = new HelpClientListener(symClient);
            helpBotSession.setHelpClientListener(helpClientListener);

            memberCommandListener = new MemberCommandListener(helpBotSession);


            symClient.getPresenceService().registerPresenceListener(new MemberPresenceListener());

            Thread inactivityThread = new InactivityThread();
            inactivityThread.start();




            MemberCommandListener listener = new MemberCommandListener(helpBotSession);
            Chat OURCHAT = new Chat();

            OURCHAT.registerListener(listener);
            OURCHAT.registerListener(listener);
            OURCHAT.registerListener(listener);
            OURCHAT.registerListener(listener);
            OURCHAT.registerListener(listener);


            System.out.println("Help desk bot is alive, and ready to help!");

        } catch (Exception e) {

            if (logger != null)
                logger.error(e.toString());
            else
                e.printStackTrace();

        }
    }

    private void addAdmin() {

        User user = null;

        try {

            user = symClient.getUsersClient().getUserFromEmail(System.getProperty(HelpBotConfig.ADMIN_USER));

            if (MemberCache.getMember(user) == null) {

                Member member = new Member(user.getEmailAddress(), user.getId());

                MemberCache.addMember(member);
                MemberCache.writeMember(member);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Initializes a connection between the bot and the symphony client.
     */
    public void initConnection() {


        try {

            symClient = new SymphonyBasicClient();

            logger.debug("{} {}", System.getProperty(HelpBotConfig.SESSIONAUTH_URL),
                    System.getProperty("keyauth.url"));
            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));

            authClient.setKeystores(
                    System.getProperty(HelpBotConfig.TRUSTSTORE_FILE),
                    System.getProperty("truststore.password"),
                    System.getProperty("certs.dir") + System.getProperty("bot.user") + ".p12",
                    System.getProperty("keystore.password"));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    System.getProperty("bot.user") + "@markit.com",
                    System.getProperty("symphony.agent.agent.url"),
                    System.getProperty("symphony.agent.pod.url")
            );

        } catch (Exception e) {

            if (logger != null)
                logger.error("Init Exception", e);
            else
                e.printStackTrace();

        }

    }

    /**
     * A method that is called by the listener when a new web is created.
     * On a new web, determine if the remote user is a member or client.
     * Register the web to the appropriate listener.
     *
     * @param chat the new web
     */
    public void onNewChat(Chat chat) {

        if (chat != null) {
            logger.debug("New web connection: " + chat.getStream());

            Set<User> users = chat.getRemoteUsers();
            if (users != null && users.size() == 1) {
                User user = users.iterator().next();

                if (user != null && MemberCache.hasMember(user.getId().toString())) {

                    memberCommandListener.listenOn(chat);
                    MemberCache.getMember(user).setOnline(true);
                    Messenger.sendMessage("Joined help desk as member.",
                            MessageSubmission.FormatEnum.TEXT, chat, symClient);

                } else if (user != null) {

                    HoldCache.putClientOnHold(ClientCache.addClient(user));
                    helpClientListener.listenOn(chat);
                    Messenger.sendMessage("Joined help desk as help client.",
                            MessageSubmission.FormatEnum.TEXT, chat, symClient);

                }

            }

        } else if (logger != null) {
            logger.warn("Incoming new web received a null value.");
        }
    }

    /**
     * A method called by the listener when a web is removed.
     * On web remove, determine if the user is a client or member.
     * Remove the web from the appropriate listener.
     *
     * @param chat the removed web
     */
    public void onRemovedChat(Chat chat) {
        logger.debug("Removed web connection: " + chat.getStream());

        if (chat != null) {
            Set<User> users = chat.getRemoteUsers();
            if (users != null && users.size() > 1) {
                User user = chat.getRemoteUsers().iterator().next();

                if (user != null && MemberCache.MEMBERS.containsKey(user.getEmailAddress())
                        && !MemberCache.MEMBERS.get(user.getEmailAddress()).isOnCall()) {

                    memberCommandListener.stopListening(chat);
                    MemberCache.getMember(user).setOnline(false);

                } else if (user != null && ClientCache.retrieveClient(user).isOnCall()) {

                    helpClientListener.stopListening(chat);
                    ClientCache.removeClient(user);

                }
            }
        } else if (logger != null) {
            logger.warn("Incoming new web received a null value.");
        }
    }
}

