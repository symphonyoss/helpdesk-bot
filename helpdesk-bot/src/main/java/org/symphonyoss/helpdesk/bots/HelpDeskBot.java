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
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.models.BotResponse;
import org.symphonyoss.helpdesk.models.HelloResponse;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserPresence;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class HelpDeskBot implements ChatListener,ChatServiceListener, PresenceListener {

    private Logger logger = LoggerFactory.getLogger(HelpDeskBot.class);

    public HelpDeskBot() {

        testIt();

    }
    public static void main(String[] args) {

        System.out.println("HelpDeskBot starting...");
        new HelpDeskBot();

    }

    public void testIt() {

//        -Dkeystore.password=SymphonyIsGreat123
//        -Dtruststore.password=SymphonyIsGreat123
//        -Dsessionauth.url=https://localhost.symphony.com:844/sessionauth
//        -Dkeyauth.url=https://localhost.symphony.com:8444/keyauth
//        -Dsymphony.agent.pod.url=https://symagent.mdevlab.com:8446/pod
//        -Dsymphony.agent.agent.url=https://symagent.mdevlab.com:8446/agent
//        -Dcerts.dir=/dev/certs/
//        -Dtruststore.file=/dev/certs/server.truststore
//        -Dbot.user=hashtag.bot

        try{

            SymphonyClient symClient = new SymphonyBasicClient();

            logger.debug("{} {}",  System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url") );
            AuthorizationClient authClient = new AuthorizationClient(
                    System.getProperty("sessionauth.url"),
                    System.getProperty("keyauth.url") );


            authClient.setKeystores(
                    System.getProperty("truststore.file"),
                    System.getProperty("truststore.password"),
                    System.getProperty("certs.dir") + System.getProperty("bot.user") + ".p12",
                    System.getProperty("keystore.password"));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    System.getProperty("bot.user") + "@markit.com" ,
                    System.getProperty("symphony.agent.agent.url"),
                    System.getProperty("symphony.agent.pod.url")
                    );

            symClient.getPresenceService().registerPresenceListener(this);
            symClient.getChatService().registerListener(this);


            MessageSubmission aMessage = new MessageSubmission();
            aMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
            aMessage.setMessage("Hello, I am the help desk BOT, here at your service.");


           // symClient.getMessageService().sendMessage("hershal.shah@markit.com", aMessage);
           // symClient.getMessageService().sendMessage("frank.tarsillo@markit.com", aMessage);

            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            Set<User> remoteUsers = new HashSet<User>();
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail("frank.tarsillo@markit.com"));
            chat.setRemoteUsers(remoteUsers);
            chat.registerListener(this);
            chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));


            symClient.getChatService().addChat(chat);

            symClient.getMessageService().sendMessage(chat, aMessage);


            logger.debug("Presence for user {} is: {}", remoteUsers, symClient.getPresenceService().getUserPresence("frank.tarsillo@markit.com"));

            BotResponseListener responseListener = new BotResponseListener(symClient);
            chat.registerListener(responseListener);
            symClient.getChatService().registerListener(responseListener);

            HelloResponse hashtag = new HelloResponse("Add", 3);
            hashtag.setPrefixRequirement(0, "#");
            hashtag.setPlaceHolder(0, "hashtag");
            hashtag.setPlaceHolder(1, "definition");

            responseListener.getActiveResponses().add(hashtag);



        }catch(Exception e){
            e.printStackTrace();
        }

    }






    public void onUserPresence(UserPresence userPresence) {

        logger.debug("Received user presence update: {}:{}", userPresence.getUid(), userPresence.getCategory());


    }

    public void onChatMessage(Message message) {

        logger.debug("TS: {}\nFrom ID: {}\nMessage: {}\nMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());

    }

    public void onNewChat(Chat chat) {

        chat.registerListener(this);

        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getId(), chat.getRemoteUsers());
    }

    public void onRemovedChat(Chat chat) {
        chat.removeListener(this);
    }
}

