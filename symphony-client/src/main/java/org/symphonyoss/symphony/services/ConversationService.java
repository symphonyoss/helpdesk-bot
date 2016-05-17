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
import org.symphonyoss.symphony.model.Conversation;
import org.symphonyoss.symphony.service.model.PresenceList;
import org.symphonyoss.symphony.service.model.User;
import org.symphonyoss.symphony.service.model.UserPresence;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Frank Tarsillo on 5/16/2016.
 */
public class ConversationService implements PresenceListener, MessageListener {

    private PresenceList presenceList;
    private ConcurrentHashMap<Long, ConversationWorker> activeConversations = new ConcurrentHashMap<Long, ConversationWorker>();
    private SymphonyClient symClient;
    private Logger logger = LoggerFactory.getLogger(ConversationService.class);
    private ArrayList<MessageListener> messageListeners;


    public ConversationService(SymphonyClient symClient) throws Exception {
        this.symClient = symClient;
        messageListeners = new ArrayList<MessageListener>();

        try {
            presenceList = symClient.getPresenceService().getAllUserPresence();
            symClient.getPresenceService().registerPresenceListener(this);

        } catch (Exception e) {

            throw new Exception("Presence retrieval failure", e);

        }

        init();

    }


    public void init() {


        for (UserPresence presence : presenceList) {

            checkUpdate(presence);

        }


    }

    public void onUserPresence(UserPresence userPresence) {
        checkUpdate(userPresence);

    }

    private void checkUpdate(UserPresence presence) {


        if (presence.getCategory() != UserPresence.CategoryEnum.AVAILABLE) {

            try {
                activeConversations.remove(presence.getUid()).kill();
            } catch (Exception e) {
            }

            return;
        }

        try {
            if (activeConversations.get(presence.getUid()) == null) {

                User remoteUser = symClient.getServiceClient().getUserFromId(presence.getUid());
                logger.debug("NEW WORKER FOR USERID: {} {}", remoteUser.getId(), remoteUser.getEmailAddress());


                Conversation conv = new Conversation();
                conv.setLocalUser(symClient.getLocalUser());
                conv.setRemoteUser(remoteUser);
                conv.setStream(symClient.getServiceClient().getStream(remoteUser));


                ConversationWorker conversationWorker = new ConversationWorker(symClient, conv, this);
                activeConversations.put(remoteUser.getId(), conversationWorker);
                new Thread(conversationWorker).start();


            }
        } catch (Exception e) {

            logger.error("Exception: {} {}", e.toString(), e.getCause());
            e.printStackTrace();
        }

    }


    public void onMessage(Message message) {

        for (MessageListener messageListener : messageListeners) {
            messageListener.onMessage(message);
        }


    }

    public void registerMessageListener(MessageListener messageListener) {

        messageListeners.add(messageListener);

    }

    public void removeMessageListeners(MessageListener messageListener) {

        messageListeners.remove(messageListener);

    }

    public Conversation getConversationByEmail(String email) {

        try {
            User user = symClient.getServiceClient().getUserFromEmail(email);
            if (user != null) {

                if (activeConversations.get(user.getId()) != null)
                    return activeConversations.get(user.getId()).getConversation();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
