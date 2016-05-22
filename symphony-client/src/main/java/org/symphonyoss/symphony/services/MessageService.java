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
import org.symphonyoss.symphony.model.Chat;
import org.symphonyoss.symphony.service.model.Stream;
import org.symphonyoss.symphony.service.model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class MessageService implements MessageListener{

    private SymphonyClient symClient;
    private org.symphonyoss.symphony.agent.invoker.ApiClient agentClient;
    private Logger logger = LoggerFactory.getLogger(MessageService.class);
    MessageFeedWorker messageFeedWorker;
    Set<MessageListener> messageListeners = new HashSet<MessageListener>();

    public MessageService(SymphonyClient symClient){

        this.symClient = symClient;

        messageFeedWorker = new MessageFeedWorker(symClient, this);
        new Thread(messageFeedWorker).start();

    }



    public void sendMessage(Chat chat, MessageSubmission message) throws Exception {

        symClient.getAgentClient().sendMessage(chat.getStream(),message);

    }


    public void sendMessage(String email, MessageSubmission message) throws Exception {

        User remoteUser = symClient.getPodClient().getUserFromEmail(email);

        symClient.getAgentClient().sendMessage(symClient.getPodClient().getStream(remoteUser),message);

    }


    public MessageList getMessagesFromStream(Stream stream, Long since, Integer offset, Integer maxMessages) throws Exception {

        return symClient.getAgentClient().getMessagesFromStream(
                stream, since, offset, maxMessages);

    }


    public MessageList getMessagesFromUserId(long userId, Long since, Integer offset, Integer maxMessages) throws Exception {


        User user = new User();
        user.setId(userId);


        return getMessagesFromStream(
                symClient.getPodClient().getStream(user), since, offset, maxMessages);


    }



    public void onMessage(Message message) {

        for(MessageListener messageListener: messageListeners){
            if(messageListener !=null)
                messageListener.onMessage(message);
        }
        logger.debug("TS: {}\nFrom ID: {}\nMessage: {}\nType: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());



    }

    public boolean registerMessageListener(MessageListener messageListener) {

        return messageListeners.add(messageListener);

    }
    public boolean removeMessageListener(MessageListener messageListener) {

        return messageListeners.remove(messageListener);

    }


}
