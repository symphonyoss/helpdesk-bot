/*
 *
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.symphonyoss.helpdesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.command.HelpClientCommandListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.*;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 * The main listener for dealing with clients requesting help
 */
public class HelpClientListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(AiCommandListener.class);
    private AiCommandListener helpResponseListener;
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient) {
        this.symClient = symClient;
        helpResponseListener = new HelpClientCommandListener(symClient);
    }

    /**
     * A method called from the chat listener, when a new chat message is received.
     * If the received message is not a command, relay the message to the onlines, off call
     * members.
     * @param message   the received message
     */
    public void onChatMessage(Message message) {
        if (message == null
                || message.getStream() == null
                || helpResponseListener.isCommand(message)) {
            if(logger != null)
                logger.warn("Ignored message {}.", message);
            return;
        }
        logger.debug("Client {} sent help request message.", message.getFromUserId());

        if (!HoldCache.hasClient(ClientCache.retrieveClient(message)))
            HoldCache.putClientOnHold(ClientCache.retrieveClient(message));

        MlMessageParser mlMessageParser;

        try {

            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());

        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }

        String[] chunks = mlMessageParser.getTextChunks();

        HelpClient helpClient = ClientCache.retrieveClient(message);
        if(helpClient != null) {
            helpClient.getHelpRequests().add(mlMessageParser.getText());
            relayToMembers(helpClient, message, chunks);

        }else{
            logger.warn("Ignored message with from id {}. " +
                    "Client could not be found.", message.getFromUserId());
        }
    }

    /**
     * Register this listener to the chat appropriately
     * @param chat the chat to register this listener on
     */
    public void listenOn(Chat chat) {
        if(chat != null){

            helpResponseListener.listenOn(chat);
            chat.registerListener(this);

        }else{
            logChatError(chat, new NullPointerException());
        }
    }

    /**
     * Remove this listener from the chat appropriately
     * @param chat the chat to remove this listener from
     */
    public void stopListening(Chat chat) {
        if(chat != null){

            helpResponseListener.stopListening(chat);
            chat.removeListener(this);

        }else{
            logChatError(chat, new NullPointerException());
        }
    }

    private void relayToMembers(HelpClient helpClient, Message message, String[] chunks){

        for (Member member : MemberCache.getBestMembers(String.join(" ", chunks))) {
            if (!member.isOnCall() && member.isSeeHelpRequests()) {

                if (helpClient.getEmail() != null &&
                        !helpClient.getEmail().equals("")) {

                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + ClientCache.retrieveClient(message).getEmail() +
                                    ": " + MLTypes.END_BOLD + String.join(" ", chunks) + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);

                } else {

                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + message.getFromUserId() + ": " + MLTypes.END_BOLD
                                    + String.join(" ", chunks) + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);

                }

            }
        }

    }

    public void logChatError(Chat chat, Exception e) {
        if (logger != null) {

            if (chat == null) {
                logger.error("Ignored method call. Chat was null value.", e);

            } else if (chat.getStream() == null) {
                logger.error("Could not put stream in push hash. " +
                        "Chat stream was null value.", e);

            } else if (chat.getStream().getId() == null) {
                logger.error("Could not put stream in push hash. " +
                        "Chat stream id was null value.", e);
            }

        }
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }



}
