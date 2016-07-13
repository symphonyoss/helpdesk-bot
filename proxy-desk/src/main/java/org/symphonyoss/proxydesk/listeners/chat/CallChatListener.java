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

package org.symphonyoss.proxydesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.proxydesk.models.calls.Call;
import org.symphonyoss.proxydesk.models.users.DeskUser;
import org.symphonyoss.proxydesk.utils.DeskUserCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.HashMap;

/**
 * Created by nicktarsillo on 6/21/16.
 * A web listener for calls between members and clients.
 */
public class CallChatListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(CallChatListener.class);
    private HashMap<String, Boolean> entered = new HashMap<String, Boolean>();
    private Call call;
    private AiCommandListener callCommandListener;
    private SymphonyClient symClient;

    public CallChatListener(Call call, AiCommandListener callCommandListener, SymphonyClient symClient) {
        this.symClient = symClient;
        this.callCommandListener = callCommandListener;
        this.call = call;
    }

    /**
     * A method called by the web listener, when a new message is received.
     * On new web message, if the message is not a command, relay the message
     * between both parties.
     *
     * @param message the received message
     */
    public void onChatMessage(Message message) {
        if (message == null
                || message.getStreamId() == null
                || (callCommandListener != null && AiCommandListener.isCommand(message, symClient))
                || isPushMessage(message)) {

            if (logger != null)
                logger.warn("Ignored message {}.", message);

            return;
        }


        MlMessageParser mlMessageParser;

        try {

            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());

        } catch (Exception e) {

            if (logger != null)
                logger.error("Could not parse message {}", message.getMessage(), e);

            return;
        }

        String text = mlMessageParser.getText();

        DeskUser deskUser = null;
        if (message.getFromUserId() != null)
            deskUser = DeskUserCache.getDeskUser(message.getFromUserId().toString());

        if (deskUser != null) {

            relayMessage(deskUser, text);

        } else {

            if (logger != null)
                logger.warn("Ignored message. Desk user {} could not be found.",
                        message.getFromUserId());

        }

        call.setInactivityTime(0);
    }

    /**
     * Registers this listener to the web.
     * Then set entered to true, based on the stream.
     *
     * @param chat the web to register listeners to
     */
    public void listenOn(Chat chat) {

        if (chat != null) {

            chat.registerListener(this);

            if (chat.getStream() != null
                    && chat.getStream().getId() != null)
                entered.put(chat.getStream().getId().toString(), true);

            else {
                logChatError(chat, new NullPointerException());
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

    }

    /**
     * Remove this listener to the web.
     * Then set entered to false, based on the stream.
     *
     * @param chat the web to remove listeners from
     */
    public void stopListening(Chat chat) {

        if (chat != null) {

            chat.removeListener(this);

            if (chat.getStream() != null
                    && chat.getStream().getId() != null)
                entered.put(chat.getStream().getId().toString(), false);

            else {
                logChatError(chat, new NullPointerException());
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

    }

    /**
     * Determine if received message is a push message.
     * (Push message occurs when a listener is registered)
     *
     * @param message the received message
     * @return if the message is a push message
     */
    private boolean isPushMessage(Message message) {
        return (entered.get(message.getStreamId()) == null
                || !entered.get(message.getStreamId()));
    }

    /**
     * Send the message sent from a member to both parties.
     * Retain the identity preference of the member.
     *
     * @param deskUser the desk user
     * @param text     the message sent from the member
     */
    private void relayMessage(DeskUser deskUser, String text) {

        for (DeskUser d : call.getDeskUsers()) {

            if (d != deskUser) {
                Messenger.sendMessage(MLTypes.START_ML.toString() + constructRelayMessage(deskUser, text) + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, d.getUserID(), symClient);
            }

        }
    }

    protected String constructRelayMessage(DeskUser deskUser, String text) {
        if (deskUser.getEmail() != null) {

            return MLTypes.START_BOLD.toString()
                    + deskUser.getEmail() + ": " + MLTypes.END_BOLD + text;

        } else {

            return MLTypes.START_BOLD.toString()
                    + deskUser.getUserID() + ": " + MLTypes.END_BOLD + text;

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


    public void setCallCommandListener(AiCommandListener callCommandListener) {
        this.callCommandListener = callCommandListener;
    }
}
