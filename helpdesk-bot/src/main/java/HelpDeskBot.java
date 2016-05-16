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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.SymphonyClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.SymphonyBasicClient;
import org.symphonyoss.symphony.model.Conversation;
import org.symphonyoss.symphony.service.model.User;
import org.symphonyoss.symphony.services.ConversationWorker;
import org.symphonyoss.symphony.model.SymAuth;
import org.symphonyoss.symphony.service.model.PresenceList;
import org.symphonyoss.symphony.service.model.UserPresence;
import org.symphonyoss.symphony.services.MessageListener;
import org.symphonyoss.symphony.services.PresenceListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class HelpDeskBot implements MessageListener, PresenceListener {

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

            symClient.init(symAuth, "https://localhost:8446/agent", "https://localhost:8446/pod");

            User localUser = symClient.getServiceClient().getUserFromEmail("bot.user1@markit.com");



            ConcurrentHashMap<Long, ConversationWorker> activeConversations = new ConcurrentHashMap<Long, ConversationWorker>();

            PresenceList presenceList;
            while (true) {

                try {
                    presenceList = symClient.getPresenceService().getAllUserPresence();

                } catch (Exception e) {

                    logger.error("Presence retrieval failure", e);
                    continue;
                }

                for (UserPresence presence : presenceList) {


                    if (presence.getCategory() != UserPresence.CategoryEnum.AVAILABLE) {

                        try {
                            activeConversations.remove(presence.getUid()).kill();
                        } catch (Exception e) {
                        }

                        continue;
                    }


                    if (activeConversations.get(presence.getUid()) == null) {

                        User remoteUser = symClient.getServiceClient().getUserFromId(presence.getUid());
                        logger.debug("NEW WORKER FOR USERID: {} {}", remoteUser.getId(), remoteUser.getEmailAddress());

                        Conversation conv = new Conversation();
                        conv.setLocalUser(localUser);
                        conv.setRemoteUser(remoteUser);
                        conv.setStream(symClient.getServiceClient().getStream(remoteUser));

                        ConversationWorker conversationWorker = new ConversationWorker(symClient, conv, this);
                        activeConversations.put(remoteUser.getId(), conversationWorker);
                        new Thread(conversationWorker).start();

//                        MessageSubmission aMessage = new MessageSubmission();
//                        aMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
//                        aMessage.setMessage("Hello, I am the help desk BOT, here at your service..");
//                        conversationWorker.send(aMessage);
                    }


                }
                TimeUnit.SECONDS.sleep(2);


            }

        } catch (Exception e) {

            logger.error("Exception: {} {}", e.toString(), e.getCause());
            e.printStackTrace();

        }



    }

    public static void main(String[] args) {

        new HelpDeskBot();

    }


    public void onMessage(Message message) {


        System.out.println("TS: " + message.getTimestamp());
        System.out.println("From ID: " + message.getFromUserId());
        System.out.println("Message: " + message.getMessage());
        System.out.println("Message Type: " + message.getMessageType());

    }

    public void onUserPresence(UserPresence userPresence) {

    }
}
