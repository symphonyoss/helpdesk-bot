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
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.SymphonyBasicClient;
import org.symphonyoss.symphony.model.SymAuth;
import org.symphonyoss.symphony.service.model.UserPresence;
import org.symphonyoss.symphony.services.MessageListener;
import org.symphonyoss.symphony.services.PresenceListener;

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

            symClient.init(symAuth, "bot.user1@markit.com" , "https://localhost:8446/agent", "https://localhost:8446/pod");

            symClient.getPresenceService().registerPresenceListener(this);
            symClient.getConversationService().registerMessageListener(this);


            MessageSubmission aMessage = new MessageSubmission();
            aMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
            aMessage.setMessage("Hello, I am the help desk BOT, here at your service..");


            symClient.getMessageService().sendMessage("hershal.shah@markit.com", aMessage);
            symClient.getMessageService().sendMessage("frank.tarsillo@markit.com", aMessage);




        }catch(Exception e){
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

        logger.debug("Received user presence update: {}:{}", userPresence.getUid(), userPresence.getCategory());


    }
}
