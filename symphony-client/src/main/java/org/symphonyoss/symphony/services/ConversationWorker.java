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

package org.symphonyoss.symphony.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.SymphonyClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageList;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.model.Conversation;


/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class ConversationWorker implements Runnable {
    SymphonyClient symphonyClient;
    Conversation conv;
    private boolean KILL = false;
    long lastTime = System.currentTimeMillis();
    MessageListener messageListener;
    private Logger logger = LoggerFactory.getLogger(ConversationWorker.class);


    public ConversationWorker(SymphonyClient symphonyClient, Conversation conv, MessageListener messageListener) {

        this.symphonyClient = symphonyClient;
        this.messageListener = messageListener;
        this.conv = conv;
    }

    public void run() {

        while (true) {
            logger.debug("Chat active for user: {} : {}", conv.getRemoteUser().getId(),conv.getStream().getId() );
            try {
                MessageList messages = symphonyClient.getMessageService().getMessagesFromStream(conv.getStream(), lastTime, 0, 50);


                if (messages != null)
                    for (Message message : messages) {

                        if (!conv.getRemoteUser().getId().equals(message.getFromUserId()))
                            continue;


                        messageListener.onMessage(message);


                        if (lastTime < Long.valueOf(message.getTimestamp()))
                            lastTime = Long.valueOf(message.getTimestamp());
                    }

                lastTime += 1;

                new Thread().sleep(2000);

                if (KILL)
                    return;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void kill() {
        logger.debug("Stopping conversation thread: {} {} ", conv.getRemoteUser().getId(), conv.getRemoteUser().getEmailAddress());
        KILL = true;


    }

    public void send(MessageSubmission message){

        try {
            symphonyClient.getMessageService().sendMessage(conv, message);
            logger.debug("Sent message to {} {}: {}", conv.getRemoteUser().getEmailAddress(),
                    conv.getRemoteUser().getEmailAddress(), message.getMessage());
        }catch(Exception e){
            logger.error("Could not send message to {} {}", conv.getRemoteUser().getEmailAddress(), conv.getRemoteUser().getEmailAddress());

        }
    }

}
