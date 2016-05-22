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

package org.symphonyoss.helpdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.SymphonyClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.SymphonyBasicClient;
import org.symphonyoss.symphony.model.Chat;
import org.symphonyoss.symphony.model.SymAuth;
import org.symphonyoss.symphony.service.model.User;
import org.symphonyoss.symphony.service.model.UserPresence;
import org.symphonyoss.symphony.services.ChatListener;
import org.symphonyoss.symphony.services.ChatServiceListener;
import org.symphonyoss.symphony.services.PresenceListener;

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


    public void testIt() {

        try {
            SymphonyClient symClient = new SymphonyBasicClient();

            AuthorizationClient authClient = new AuthorizationClient("https://localhost.symphony.com:8444/sessionauth",
                    "https://localhost.symphony.com:8444/keyauth");
            authClient.setKeystores("/dev/certs/server.truststore", System.getProperty("keystore.password"),
                    "/dev/certs/bot.user1.p12", System.getProperty("keystore.password"));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(symAuth, "bot.user1@markit.com" , "https://localhost:8446/agent", "https://localhost:8446/pod");

            symClient.getPresenceService().registerPresenceListener(this);
            symClient.getChatService().registerListener(this);


            MessageSubmission aMessage = new MessageSubmission();
            aMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
            aMessage.setMessage("Hello, I am the help desk BOT, here at your service..");


           // symClient.getMessageService().sendMessage("hershal.shah@markit.com", aMessage);
           // symClient.getMessageService().sendMessage("frank.tarsillo@markit.com", aMessage);

            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            Set<User> remoteUsers = new HashSet<User>();
            remoteUsers.add(symClient.getPodClient().getUserFromEmail("frank.tarsillo@markit.com"));
            chat.setRemoteUsers(remoteUsers);
            chat.registerListener(this);
            chat.setStream(symClient.getPodClient().getStream(remoteUsers));


            symClient.getChatService().addChat(chat);

            symClient.getMessageService().sendMessage(chat,aMessage);


            logger.debug("Presence for user {} is: {}", remoteUsers, symClient.getPresenceService().getUserPresence("frank.tarsillo@markit.com"));





        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        new HelpDeskBot();

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

    }
}
