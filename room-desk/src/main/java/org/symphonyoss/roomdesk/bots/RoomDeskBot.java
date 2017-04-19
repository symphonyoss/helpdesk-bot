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
package org.symphonyoss.roomdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.roomdesk.config.RoomBotConfig;
import org.symphonyoss.roomdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.roomdesk.listeners.chat.MemberAliasListener;
import org.symphonyoss.roomdesk.listeners.chat.TranscriptListener;
import org.symphonyoss.roomdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.roomdesk.models.HelpBotSession;
import org.symphonyoss.roomdesk.models.users.Member;
import org.symphonyoss.roomdesk.utils.ClientCache;
import org.symphonyoss.roomdesk.utils.DeskUserCache;
import org.symphonyoss.roomdesk.utils.HoldCache;
import org.symphonyoss.roomdesk.utils.MemberCache;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.Stream;

import java.util.HashSet;
import java.util.Set;

import static org.symphonyoss.roomdesk.config.RoomBotConfig.Config;

/**
 * The main help desk bot class
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
public class RoomDeskBot implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(RoomDeskBot.class);
    private MemberCommandListener memberCommandListener;
    private MemberAliasListener memberAliasListener;
    private HelpClientListener helpClientListener;
    private TranscriptListener transcriptListener;
    private SymphonyClient symClient;

    public RoomDeskBot() {
        logger.info("Init for help desk user {}", Config.getString(RoomBotConfig.BOT_USER));
        initConnection();
        setupBot();
        setupChat();

        logger.debug(System.getProperty(RoomBotConfig.MEMBER_CHAT_STREAM));

    }

    public static void main(String[] args) {
        System.out.println("RoomDeskBot starting...");

        new RoomDeskBot();
    }

    /**
     * Sets up the bot.
     * Loads all the members from file into the cache.
     * Registers chat service.
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

            memberCommandListener = new MemberCommandListener(helpBotSession);

            memberAliasListener = new MemberAliasListener(symClient);

            System.out.println("Help desk bot is alive, and ready to help!");

        } catch (Exception e) {

            if (logger != null)
                logger.error(e.toString());
            else
                e.printStackTrace();

        }
    }

    private void setupChat() {

        Chat chat = new Chat();
        Stream stream = new Stream();
        stream.setId(System.getProperty(RoomBotConfig.MEMBER_CHAT_STREAM));
        chat.setStream(stream);

        chat.setRemoteUsers(new HashSet<SymUser>());

        Messenger.sendMessage("Ready to help!",
                SymMessage.Format.TEXT, chat, symClient);

        chat.registerListener(transcriptListener);
        symClient.getChatService().addChat(chat);
    }

    private void addAdmin() {

        SymUser user = null;

        try {

            user = symClient.getUsersClient().getUserFromEmail(System.getProperty(RoomBotConfig.ADMIN_USER));

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

//        -Dkeystore.password=SymphonyIsGreat123
//        -Dtruststore.password=SymphonyIsGreat123
//        -Dsessionauth.url=https://localhost.symphony.com:844/sessionauth
//        -Dkeyauth.url=https://localhost.symphony.com:8444/keyauth
//        -Dsymphony.agent.pod.url=https://symagent.mdevlab.com:8446/pod
//        -Dsymphony.agent.agent.url=https://symagent.mdevlab.com:8446/agent
//        -Dcerts.dir=/dev/certs/
//        -Dtruststore.file=/dev/certs/server.truststore
//        -Dbot.user=hashtag.bot

        try {

            symClient = new SymphonyBasicClient();

            logger.debug("{} {}", System.getProperty(RoomBotConfig.SESSIONAUTH_URL),
                    System.getProperty(RoomBotConfig.KEYAUTH_URL));
            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty(RoomBotConfig.SESSIONAUTH_URL),
                    System.getProperty(RoomBotConfig.KEYAUTH_URL));

            authClient.setKeystores(
                    System.getProperty(RoomBotConfig.TRUSTSTORE_FILE),
                    System.getProperty(RoomBotConfig.TRUSTSTORE_PASSWORD),
                    System.getProperty(RoomBotConfig.CERTS_DIR) + System.getProperty(RoomBotConfig.BOT_USER) + ".p12",
                    System.getProperty(RoomBotConfig.KEYSTORE_PASSWORD));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    System.getProperty(RoomBotConfig.BOT_USER) + "@" + System.getProperty(RoomBotConfig.BOT_DOMAIN),
                    System.getProperty(RoomBotConfig.SYMPHONY_AGENT),
                    System.getProperty(RoomBotConfig.SYMPHONY_POD)
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
            logger.debug("New chat connection: " + chat.getStream());


            SymUser user = chat.getRemoteUsers().stream().filter(symUser1 -> !symUser1.getId().equals(symClient.getLocalUser().getId())).findFirst().orElse(null);


            try {
                if (!chat.getStream().getId().equals(symClient.getStreamsClient().getStream(user).getId()))
                    return;
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.debug("Accepted Chat: " + chat.toString());
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
            logger.warn("Incoming new chat received a null value.");
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
        logger.debug("Removed chat connection: " + chat.getStream());

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
            logger.warn("Incoming new chat received a null value.");
        }
    }
}

