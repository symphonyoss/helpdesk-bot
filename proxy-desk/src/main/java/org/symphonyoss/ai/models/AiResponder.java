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

package org.symphonyoss.ai.models;

import org.symphonyoss.ai.constants.AiConstants;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.LinkedList;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/20/16.
 * A part of the ai with the main purpose of responding back to a SymUser
 */
public class AiResponder {
    private SymphonyClient symClient;

    public AiResponder(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    /**
     * Sends a message to a SymUser
     *
     * @param message   the message received from the SymUser
     * @param type      the type of message to send
     * @param userID    the id of the SymUser
     * @param symClient the org.org.symphonyoss.ai's sym client
     */
    public void sendMessage(String message, SymMessage.Format type, Long userID, SymphonyClient symClient) {

        SymMessage userMessage = new SymMessage();
        userMessage.setFormat(type);
        userMessage.setMessage(message);

        UserIdList list = new UserIdList();
        list.add(userID);

        try {

            symClient.getMessagesClient().sendMessage(symClient.getStreamsClient().getStream(list), userMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Respond to the SymUser, based on the values and ids given in the set of responses
     *
     * @param responseLists the set of responses
     */
    public void respondWith(Set<AiResponseSequence> responseLists) {

        for (AiResponseSequence list : responseLists) {
            for (AiResponse response : list.getAiResponseSet()) {

                for (Long userID : response.getToIDs()) {
                    sendMessage(response.getMessage(), response.getType(), userID, symClient);
                }

            }
        }

    }

    /**
     * Send a message back to the SymUser, suggesting a command
     *
     * @param suggestion the suggested command
     * @param message    the message received from the SymUser
     */
    public void sendSuggestionMessage(AiLastCommand suggestion, SymMessage message) {

        sendMessage(MLTypes.START_ML + AiConstants.SUGGEST
                        + MLTypes.START_BOLD + suggestion.getMlMessageParser().getText()
                        + MLTypes.END_BOLD + AiConstants.USE_SUGGESTION + MLTypes.END_ML,
                SymMessage.Format.MESSAGEML, message.getFromUserId(), symClient);

    }

    /**
     * Sends the command usage menu back to the SymUser
     *
     * @param message         the message received from the SymUser
     * @param mlMessageParser a parser that contains the input in ML
     * @param activeCommands  the active set of commands within the org.org.symphonyoss.ai command listener
     */
    public void sendUsage(SymMessage message, MlMessageParser mlMessageParser, LinkedList<AiCommand> activeCommands) {

        SymMessage aMessage = new SymMessage();
        aMessage.setFormat(SymMessage.Format.MESSAGEML);

        String usage = MLTypes.START_ML + mlMessageParser.getText() + AiConstants.NOT_INTERPRETABLE
                + MLTypes.BREAK + MLTypes.START_BOLD
                + AiConstants.USAGE + MLTypes.END_BOLD + MLTypes.BREAK;

        for (AiCommand command : activeCommands) {

            if (command.userIsPermitted(message.getFromUserId())) {
                usage += command.toMLCommand();
            }

        }

        usage += MLTypes.END_ML;

        sendMessage(usage, SymMessage.Format.MESSAGEML, message.getFromUserId(), symClient);

    }

    /**
     * Send a message back to the SymUser, informing them that they do not have the
     * required permission
     *
     * @param message the message reveived back from the SymUser
     */
    public void sendNoPermission(SymMessage message) {

        Messenger.sendMessage(AiConstants.NO_PERMISSION,
                SymMessage.Format.TEXT, message, symClient);

    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
