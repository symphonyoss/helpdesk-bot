/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.symphonyoss.helpdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.listeners.command.MemberResponseListener;
import org.symphonyoss.helpdesk.listeners.presence.MemberPresenceListener;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.User;

import java.util.Set;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class HelpDeskBot implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(HelpDeskBot.class);
    private AiCommandListener memberResponseListener;
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;

    public HelpDeskBot() {
        initConnection();
        setupBot();
    }

    public static void main(String[] args) {
        System.out.println("HelpDeskBot starting...");
        new HelpDeskBot();
    }

    public void setupBot() {
        try {
            MemberCache.loadMembers();

            symClient.getChatService().registerListener(this);
            helpClientListener = new HelpClientListener(symClient);
            memberResponseListener = new MemberResponseListener(symClient, helpClientListener);

            symClient.getPresenceService().registerPresenceListener(new MemberPresenceListener());

            Thread inactivityThread = new InactivityThread();
            inactivityThread.start();

            System.out.println("Help desk bot is alive, and ready to help!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));
            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url"));


            authClient.setKeystores(
                    System.getProperty("truststore.file"),
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
            e.printStackTrace();
        }

    }

    public void onNewChat(Chat chat) {
        try {
            if (chat != null) {
                logger.debug("New chat connection: " + chat.getStream());
                Set<User> users = chat.getRemoteUsers();
                if (users != null && users.size() == 1) {
                    User user = users.iterator().next();
                    if (MemberCache.hasMember(user.getId().toString())) {
                        memberResponseListener.listenOn(chat);
                        MemberCache.getMember(user).setOnline(true);
                        Messenger.sendMessage("Joined help desk as member.",
                                MessageSubmission.FormatEnum.TEXT, chat, symClient);
                    } else {
                        HoldCache.putClientOnHold(ClientCache.addClient(user));
                        helpClientListener.listenOn(chat);
                        Messenger.sendMessage("Joined help desk as help client.",
                                MessageSubmission.FormatEnum.TEXT, chat, symClient);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRemovedChat(Chat chat) {
        logger.debug("Removed chat connection: " + chat.getStream());
        User user = chat.getRemoteUsers().iterator().next();
        if (MemberCache.MEMBERS.containsKey(user.getEmailAddress())
                && !MemberCache.MEMBERS.get(user.getEmailAddress()).isOnCall()) {
            chat.removeListener(memberResponseListener);
            MemberCache.getMember(user).setOnline(false);
        } else if (ClientCache.retrieveClient(user).isOnCall()) {
            chat.removeListener(helpClientListener);
            ClientCache.removeClient(user);
        }
    }
}

