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
package org.symphonyoss.webdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.webdesk.config.WebBotConfig;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webdesk.listeners.chat.MemberAliasListener;
import org.symphonyoss.webdesk.listeners.chat.TranscriptListener;
import org.symphonyoss.webdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.webdesk.listeners.web.WebHelpSessionListener;
import org.symphonyoss.webdesk.listeners.web.WebRegistrationListener;
import org.symphonyoss.webdesk.models.HelpBotSession;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.utils.ClientCache;
import org.symphonyoss.webdesk.utils.DeskUserCache;
import org.symphonyoss.webdesk.utils.HoldCache;
import org.symphonyoss.webdesk.utils.MemberCache;
import org.symphonyoss.webservice.SymphonyWebService;
import org.symphonyoss.webservice.listeners.WebSessionListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.symphonyoss.webdesk.config.WebBotConfig.Config;

/**
 * The main help desk bot class.
 * <p>
 * <p>
 * /**
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
 */
public class WebDeskBot implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(WebDeskBot.class);
    private MemberCommandListener memberCommandListener;
    private MemberAliasListener memberAliasListener;
    private HelpClientListener helpClientListener;
    private WebSessionListener webSessionListener;
    private TranscriptListener transcriptListener;
    private SymphonyClient symClient;

    public WebDeskBot() {
        logger.info("Init for help desk user {}", Config.getString(WebBotConfig.BOT_USER));
        initConnection();
        setupBot();
        setupWeb();
        setupChat();

        logger.debug(System.getProperty(WebBotConfig.MEMBER_CHAT_STREAM));

    }

    public static void main(String[] args) {
        System.out.println("WebDeskBot starting...");

        new WebDeskBot();
    }

    /**
     * Sets up the bot.
     * Loads all the members from file into the cache.
     * Registers web service.
     * Instantiates web listeners.
     * Instantiates chat listeners.
     * Starts threads (inactivity).
     */
    public void setupBot() {
        try {

            MemberCache.loadMembers();

            addAdmin();

            HelpBotSession helpBotSession = new HelpBotSession();

            transcriptListener = new TranscriptListener(symClient);
            helpBotSession.setTranscriptListener(transcriptListener);

            symClient.getChatService().registerListener(this);
            helpBotSession.setSymphonyClient(symClient);

            helpClientListener = new HelpClientListener(symClient);
            helpBotSession.setHelpClientListener(helpClientListener);

            webSessionListener = new WebHelpSessionListener(symClient);

            helpBotSession.setHelpSession(webSessionListener);

            memberCommandListener = new MemberCommandListener(helpBotSession);

            memberAliasListener = new MemberAliasListener(symClient);

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
     * Start the web service
     */
    private void setupWeb() {
        SymphonyWebService webService = new SymphonyWebService();

        webService.registerListener(new WebRegistrationListener(symClient, webSessionListener, helpClientListener));
    }

    /**
     * Setup the member chat room
     */
    private void setupChat() {

        Chat chat = new Chat();
        Stream stream = new Stream();
        stream.setId(System.getProperty(WebBotConfig.MEMBER_CHAT_STREAM));
        chat.setStream(stream);

        chat.setRemoteUsers(new HashSet<SymUser>());

        Messenger.sendMessage("Ready to help!",
                SymMessage.Format.TEXT, chat, symClient);

        chat.registerListener(transcriptListener);
        symClient.getChatService().addChat(chat);
    }

    /**
     * Add the admin as a member as specified in the config
     */
    private void addAdmin() {

        SymUser user = null;

        try {

            user = symClient.getUsersClient().getUserFromEmail(System.getProperty(WebBotConfig.ADMIN_USER));

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

            logger.debug("{} {}", System.getProperty(WebBotConfig.SESSIONAUTH_URL),
                    System.getProperty(WebBotConfig.KEYAUTH_URL));
            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty(WebBotConfig.SESSIONAUTH_URL),
                    System.getProperty(WebBotConfig.KEYAUTH_URL));

            authClient.setKeystores(
                    System.getProperty(WebBotConfig.TRUSTSTORE_FILE),
                    System.getProperty(WebBotConfig.TRUSTSTORE_PASSWORD),
                    System.getProperty(WebBotConfig.CERTS_DIR) + System.getProperty(WebBotConfig.BOT_USER) + ".p12",
                    System.getProperty(WebBotConfig.KEYSTORE_PASSWORD));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    System.getProperty(WebBotConfig.BOT_USER) + "@" + System.getProperty(WebBotConfig.BOT_DOMAIN),
                    System.getProperty(WebBotConfig.SYMPHONY_AGENT),
                    System.getProperty(WebBotConfig.SYMPHONY_POD)
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
     * On a new chat, determine if the remote user is a member or client.
     * Register the chat to the appropriate listener.
     *
     * @param chat the new chat
     */
    public void onNewChat(Chat chat) {

        if (chat != null) {
            logger.info("New chat: " + chat.getStream().getId());
            logger.info("Users in chat: " + Arrays.toString(chat.getRemoteUsers().toArray()));

            SymUser user = chat.getRemoteUsers().stream().filter(symUser1 -> !symUser1.getId().equals(symClient.getLocalUser().getId())).findFirst().orElse(null);


            try {
                if (!chat.getStream().getId().equals(symClient.getStreamsClient().getStream(user).getId()))
                    return;
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.info("Accepted Chat: " + chat.toString());
            if (user != null && !DeskUserCache.hasUser(user)) {
                if (MemberCache.hasMember(user.getId().toString())) {

                    memberCommandListener.listenOn(chat);
                    memberAliasListener.listenOn(chat);
                    chat.registerListener(transcriptListener);
                    MemberCache.getMember(user).setOnline(true);
                    Messenger.sendMessage("Joined help desk as member.",
                            SymMessage.Format.TEXT, chat, symClient);

                } else {

                    HoldCache.putClientOnHold(ClientCache.addClient(user));
                    helpClientListener.listenOn(chat);
                    chat.registerListener(transcriptListener);
                    Messenger.sendMessage("Joined help desk as help client.",
                            SymMessage.Format.TEXT, chat, symClient);

                }
            }


        } else if (logger != null) {
            logger.warn("Incoming new web received a null value.");
        }
    }

    /**
     * A method called by the listener when a chat is removed.
     * On chat remove, determine if the user is a client or member.
     * Remove the chat from the appropriate listener.
     *
     * @param chat the removed chat
     */
    public void onRemovedChat(Chat chat) {
        logger.debug("Removed web connection: " + chat.getStream());

        if (chat != null) {
            Set<SymUser> users = chat.getRemoteUsers();
            if (users != null && users.size() == 1) {
                SymUser user = chat.getRemoteUsers().iterator().next();

                if (user != null && MemberCache.MEMBERS.containsKey(user.getEmailAddress())
                        && !MemberCache.MEMBERS.get(user.getEmailAddress()).isOnCall()) {

                    memberCommandListener.stopListening(chat);
                    memberAliasListener.stopListenung(chat);
                    chat.removeListener(transcriptListener);
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

