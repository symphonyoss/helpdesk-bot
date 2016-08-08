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
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.LinkedList;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/20/16.
 * A part of the ai with the main purpose of responding back to a user
 */
public class AiResponder {
    private SymphonyClient symClient;

    public AiResponder(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    /**
     * Sends a message to a user
     *
     * @param message   the message received from the user
     * @param type      the type of message to send
     * @param userID    the id of the user
     * @param symClient the org.org.symphonyoss.ai's sym client
     */
    public void sendMessage(String message, MessageSubmission.FormatEnum type, Long userID, SymphonyClient symClient) {

        MessageSubmission userMessage = new MessageSubmission();
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
     * Respond to the user, based on the values and ids given in the set of responses
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
     * Send a message back to the user, suggesting a command
     *
     * @param suggestion the suggested command
     * @param message    the message received from the user
     */
    public void sendSuggestionMessage(AiLastCommand suggestion, Message message) {

        sendMessage(MLTypes.START_ML + AiConstants.SUGGEST
                        + MLTypes.START_BOLD + suggestion.getMlMessageParser().getText()
                        + MLTypes.END_BOLD + AiConstants.USE_SUGGESTION + MLTypes.END_ML,
                MessageSubmission.FormatEnum.MESSAGEML, message.getFromUserId(), symClient);

    }

    /**
     * Sends the command usage menu back to the user
     *
     * @param message         the message received from the user
     * @param mlMessageParser a parser that contains the input in ML
     * @param activeCommands  the active set of commands within the org.org.symphonyoss.ai command listener
     */
    public void sendUsage(Message message, MlMessageParser mlMessageParser, LinkedList<AiCommand> activeCommands) {

        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);

        String usage = MLTypes.START_ML + mlMessageParser.getText() + AiConstants.NOT_INTERPRETABLE
                + MLTypes.BREAK + MLTypes.START_BOLD
                + AiConstants.USAGE + MLTypes.END_BOLD + MLTypes.BREAK;

        for (AiCommand command : activeCommands) {

            if (command.userIsPermitted(message.getFromUserId())) {
                usage += command.toMLCommand();
            }

        }

        usage += MLTypes.END_ML;

        sendMessage(usage, MessageSubmission.FormatEnum.MESSAGEML, message.getFromUserId(), symClient);

    }

    /**
     * Send a message back to the user, informing them that they do not have the
     * required permission
     *
     * @param message the message reveived back from the user
     */
    public void sendNoPermission(Message message) {

        Messenger.sendMessage(AiConstants.NO_PERMISSION,
                MessageSubmission.FormatEnum.TEXT, message, symClient);

    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
