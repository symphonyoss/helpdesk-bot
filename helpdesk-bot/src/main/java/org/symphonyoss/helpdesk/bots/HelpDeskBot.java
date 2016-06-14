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

import Constants.HelpBotConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.impl.SymphonyBasicClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.HelpClientListener;
import org.symphonyoss.helpdesk.models.MemberDatabase;
import org.symphonyoss.helpdesk.models.responses.AcceptHelpResponse;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserPresence;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class HelpDeskBot implements ChatServiceListener{
    private Logger logger = LoggerFactory.getLogger(HelpDeskBot.class);
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

            helpClientListener = new HelpClientListener(symClient);
            memberResponseListener = new BotResponseListener(symClient);
            AcceptHelpResponse acceptResponse1 = new AcceptHelpResponse("Accept Next Client", 0, helpClientListener);
            AcceptHelpResponse acceptResponse2 = new AcceptHelpResponse("Accept ", 1, helpClientListener);
            acceptResponse2.setPlaceHolder(0, "Client");
            acceptResponse2.setPrefixRequirement(0, "@");

            System.out.println("Help desk bot is alive, and ready to help!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onNewChat(Chat chat) {
        Set<User> users = chat.getRemoteUsers();
        if(users.size() == 1){
            if(MemberDatabase.members.containsKey(users.iterator().next().getEmailAddress()))
                chat.registerListener(memberResponseListener);
            else{
                chat.registerListener(helpClientListener);
                User user = users.iterator().next();
                HelpBotConstants.ALLCLIENTS.put(user.getId().toString(),
                        new HelpClient(user.getEmailAddress(), user.getId()));
            }
        }
    }

    public void onRemovedChat(Chat chat) {
        User user = chat.getRemoteUsers().iterator().next();
        if(MemberDatabase.members.containsKey(user.getEmailAddress())
                && !MemberDatabase.members.get(user.getEmailAddress()).isOnCall()){
            chat.removeListener(memberResponseListener);
        }else if(!HelpBotConstants.ALLCLIENTS.get(user.getId()).isOnCall()){
            chat.removeListener(helpClientListener);
            HelpBotConstants.ALLCLIENTS.remove(user.getId());
        }
    }
}

