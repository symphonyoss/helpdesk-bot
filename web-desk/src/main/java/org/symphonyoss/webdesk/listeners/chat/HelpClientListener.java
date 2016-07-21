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

package org.symphonyoss.webdesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.webdesk.config.WebBotConfig;
import org.symphonyoss.webdesk.listeners.command.HelpClientCommandListener;
import org.symphonyoss.webdesk.models.users.HelpClient;
import org.symphonyoss.webdesk.utils.ClientCache;
import org.symphonyoss.webdesk.utils.HoldCache;

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
     * A method called from the web listener, when a new web message is received.
     * If the received message is not a command, relay the message to the members, off call
     * members.
     *
     * @param message the received message
     */
    public void onChatMessage(Message message) {
        if (message == null
                || message.getStreamId() == null
                || AiCommandListener.isCommand(message, symClient)) {
            if (logger != null)
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
        if (helpClient != null) {
            helpClient.getHelpRequests().add(mlMessageParser.getText());
            relayToMembers(helpClient, message);

        } else {
            logger.warn("Ignored message with from id {}. " +
                    "Client could not be found.", message.getFromUserId());
        }
    }

    /**
     * Register this listener to the web appropriately
     *
     * @param chat the web to register this listener on
     */
    public void listenOn(Chat chat) {
        if (chat != null) {

            helpResponseListener.listenOn(chat);
            chat.registerListener(this);

        } else {
            logChatError(chat, new NullPointerException());
        }
    }

    /**
     * Remove this listener from the web appropriately
     *
     * @param chat the web to remove this listener from
     */
    public void stopListening(Chat chat) {
        if (chat != null) {

            helpResponseListener.stopListening(chat);
            chat.removeListener(this);

        } else {
            logChatError(chat, new NullPointerException());
        }
    }

    private void relayToMembers(HelpClient helpClient, Message message) {
        Chat chat = symClient.getChatService().getChatByStream(System.getProperty(WebBotConfig.MEMBER_CHAT_STREAM));

        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD + helpClient.getEmail() + MLTypes.END_BOLD + ": " + message.getMessage().substring(MLTypes.START_ML.toString().length()),
                MessageSubmission.FormatEnum.MESSAGEML, chat, symClient);

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
