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
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.listeners.presence.MemberPresenceListener;
import org.symphonyoss.helpdesk.models.responses.AcceptHelpResponse;
import org.symphonyoss.helpdesk.models.responses.AddMemberResponse;
import org.symphonyoss.helpdesk.models.responses.ToggleIdentityResponse;
import org.symphonyoss.helpdesk.models.responses.ToggleSeeHelpResponse;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.threads.InactivityThread;
import org.symphonyoss.helpdesk.utils.ClientDatabase;
import org.symphonyoss.helpdesk.utils.HoldDesk;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class HelpDeskBot implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(HelpDeskBot.class);
    private BotResponseListener memberResponseListener;
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;

    public HelpDeskBot() {
        init();
    }

    public static void main(String[] args) {
        System.out.println("HelpDeskBot starting...");
        new HelpDeskBot();
    }

    public void init() {

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

            MemberDatabase.loadMembers();

            symClient.getChatService().registerListener(this);
            helpClientListener = new HelpClientListener(symClient);
            memberResponseListener = new BotResponseListener(symClient);

            AcceptHelpResponse acceptNextHelpClient = new AcceptHelpResponse("Accept Next Client", 0, helpClientListener);

            AcceptHelpResponse acceptHelpClient = new AcceptHelpResponse("Accept ", 1, helpClientListener);
            acceptHelpClient.setPlaceHolder(0, "Client");
            acceptHelpClient.setPrefixRequirement(0, "@");

            ToggleSeeHelpResponse toggleHelp = new ToggleSeeHelpResponse("Toggle See Help", 0);

            ToggleIdentityResponse toggleIdentity = new ToggleIdentityResponse("Toggle Show Identity", 0);

            AddMemberResponse addMember = new AddMemberResponse("Add Member", 1, helpClientListener);
            addMember.setPlaceHolder(0, "Client");
            addMember.setPrefixRequirement(0, "@");

            memberResponseListener.getActiveResponses().add(acceptNextHelpClient);
            memberResponseListener.getActiveResponses().add(acceptHelpClient);
            memberResponseListener.getActiveResponses().add(toggleHelp);
            memberResponseListener.getActiveResponses().add(toggleIdentity);
            memberResponseListener.getActiveResponses().add(addMember);

            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            Set<User> remoteUsers = new HashSet<User>();
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail(HelpBotConstants.ADMINEMAIL));
            chat.setRemoteUsers(remoteUsers);
            chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));

            symClient.getChatService().addChat(chat);
            symClient.getPresenceService().registerPresenceListener(new MemberPresenceListener());

            Thread inactivityThread = new InactivityThread();
            inactivityThread.start();

            System.out.println("Help desk bot is alive, and ready to help!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onNewChat(Chat chat) {
        try {
            logger.debug("New chat connection: " + chat.getStream());
            Set<User> users = chat.getRemoteUsers();
            if (users != null && users.size() == 1) {
                User user = users.iterator().next();
                if (user.getEmailAddress().equals(HelpBotConstants.ADMINEMAIL)
                        || MemberDatabase.MEMBERS.containsKey(user.getId())) {
                    chat.registerListener(memberResponseListener);
                    Messenger.sendMessage("Joined help desk as member.",
                            MessageSubmission.FormatEnum.TEXT, chat, symClient);
                } else {
                    HoldDesk.putClientOnHold(ClientDatabase.addClient(user));
                    chat.registerListener(helpClientListener);
                    Messenger.sendMessage("Joined help desk as help client.",
                            MessageSubmission.FormatEnum.TEXT, chat, symClient);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRemovedChat(Chat chat) {
        logger.debug("Removed chat connection: " + chat.getStream());
        User user = chat.getRemoteUsers().iterator().next();
        if (MemberDatabase.MEMBERS.containsKey(user.getEmailAddress())
                && !MemberDatabase.MEMBERS.get(user.getEmailAddress()).isOnCall()) {
            chat.removeListener(memberResponseListener);
        } else if (ClientDatabase.retrieveClient(user).isOnCall()) {
            chat.removeListener(helpClientListener);
            ClientDatabase.removeClient(user);
        }
    }
}

