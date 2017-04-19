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
package org.symphonyoss.proxydesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.proxydesk.config.ProxyBotConfig;
import org.symphonyoss.proxydesk.listeners.chat.HelpClientListener;
import org.symphonyoss.proxydesk.listeners.command.MemberCommandListener;
import org.symphonyoss.proxydesk.listeners.presence.MemberPresenceListener;
import org.symphonyoss.proxydesk.models.HelpBotSession;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.ClientCache;
import org.symphonyoss.proxydesk.utils.HoldCache;
import org.symphonyoss.proxydesk.utils.MemberCache;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.util.Set;

/**
 * The main help desk bot class
 * REQUIRED VM Arguments or System Properties:
 * <p>
 * -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 * -Dkeyauth.url=https://pod_fqdn:port/keyauth
 * -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 * -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 * -Dcerts.dir=/dev/certs/
 * -Dkeystore.password=(Pass)
 * -Dtruststore.file=/dev/certs/server.truststore
 * -Dtruststore.password=(Pass)
 * -Dbot.SymUser=hashtag.bot
 * -Dbot.domain=markit.com
 * -Dfiles.json=/dev/json
 */

public class ProxyDeskBot implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(ProxyDeskBot.class);
    private MemberCommandListener memberCommandListener;
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;


    public ProxyDeskBot() {

        logger.info("Init for help desk SymUser {}", System.getProperty(ProxyBotConfig.BOT_USER));
        initConnection();
        setupBot();
    }

    public static void main(String[] args) {
        System.out.println("ProxyDeskBot starting...");
        ProxyBotConfig.getConfig().getString("");
        new ProxyDeskBot();
    }

    /**
     * Sets up the bot.
     * Loads all the members from file into the cache.
     * Instantiates chat listeners. (Member chat, Help chat)
     * <p>
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
            helpClientListener.setPushMessages(true);
            helpBotSession.setHelpClientListener(helpClientListener);

            memberCommandListener = new MemberCommandListener(helpBotSession);


            symClient.getPresenceService().registerPresenceListener(new MemberPresenceListener());

            Thread inactivityThread = new InactivityThread();
            inactivityThread.start();


            System.out.println("Help desk bot is alive, and ready to help!");

        } catch (Exception e) {
            e.printStackTrace();
            if (logger != null)
                logger.error(e.toString());
            else
                e.printStackTrace();

        }
    }

    /**
     * Adds the admin specified in the config
     */
    private void addAdmin() {

        SymUser SymUser = null;

        try {

            SymUser = symClient.getUsersClient().getUserFromEmail(System.getProperty(ProxyBotConfig.ADMIN_USER));

            if (MemberCache.getMember(SymUser) == null) {

                Member member = new Member(SymUser.getEmailAddress(), SymUser.getId());

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

            logger.debug("{} {}", System.getProperty(ProxyBotConfig.SESSIONAUTH_URL),
                    System.getProperty(ProxyBotConfig.KEYAUTH_URL));
            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty(ProxyBotConfig.SESSIONAUTH_URL),
                    System.getProperty(ProxyBotConfig.KEYAUTH_URL));

            authClient.setKeystores(
                    System.getProperty(ProxyBotConfig.TRUSTSTORE_FILE),
                    System.getProperty(ProxyBotConfig.TRUSTSTORE_PASSWORD),
                    System.getProperty(ProxyBotConfig.CERTS_DIR) + System.getProperty(ProxyBotConfig.BOT_USER) + ".p12",
                    System.getProperty(ProxyBotConfig.KEYSTORE_PASSWORD));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    System.getProperty(ProxyBotConfig.BOT_USER) + "@" + System.getProperty(ProxyBotConfig.BOT_DOMAIN),
                    System.getProperty(ProxyBotConfig.SYMPHONY_AGENT),
                    System.getProperty(ProxyBotConfig.SYMPHONY_POD)
            );

        } catch (Exception e) {

            if (logger != null)
                logger.error("Init Exception", e);
            else
                e.printStackTrace();

        }

    }

    /**
     * A method that is called by the listener when a new chat is created.
     * On a chat, determine if the remote SymUser is a member or client.
     * Register the chat to the appropriate listener. (Member chat or HelpClient Chat)
     *
     * @param chat the new chat
     */
    public void onNewChat(Chat chat) {

        if (chat != null) {
            logger.debug("New web connection: " + chat.getStream());

            SymUser symUser = chat.getRemoteUsers().stream().filter(symUser1 -> !symUser1.getId().equals(symClient.getLocalUser().getId())).findFirst().orElse(null);

            if (symUser != null && MemberCache.hasMember(symUser.getId().toString())) {

                MemberCache.getMember(symUser).setOnline(true);
                memberCommandListener.listenOn(chat);
                Messenger.sendMessage("Joined help desk as member.",
                        SymMessage.Format.TEXT, chat, symClient);

            } else if (symUser != null) {

                HoldCache.putClientOnHold(ClientCache.addClient(symUser));
                helpClientListener.listenOn(chat);
                Messenger.sendMessage("Joined help desk as help client.",
                        SymMessage.Format.TEXT, chat, symClient);

            }


        } else if (logger != null) {
            logger.warn("Incoming new web received a null value.");
        }
    }

    /**
     * A method called by the listener when a chat is removed.
     * On chat remove, determine if the SymUser is a client or member.
     * Remove the chat from the appropriate listener. (Member chat or HelpClient Chat)
     *
     * @param chat the removed chat
     */
    public void onRemovedChat(Chat chat) {
        logger.debug("Removed web connection: " + chat.getStream());

        if (chat != null) {
            Set<SymUser> users = chat.getRemoteUsers();
            if (users != null && users.size() > 1) {
                SymUser SymUser = chat.getRemoteUsers().iterator().next();

                if (SymUser != null && MemberCache.MEMBERS.containsKey(SymUser.getEmailAddress())
                        && !MemberCache.MEMBERS.get(SymUser.getEmailAddress()).isOnCall()) {

                    memberCommandListener.stopListening(chat);
                    MemberCache.getMember(SymUser).setOnline(false);

                } else if (SymUser != null && ClientCache.retrieveClient(SymUser).isOnCall()) {

                    helpClientListener.stopListening(chat);
                    ClientCache.removeClient(SymUser);

                }
            }
        } else if (logger != null) {
            logger.warn("Incoming new web received a null value.");
        }
    }
}

