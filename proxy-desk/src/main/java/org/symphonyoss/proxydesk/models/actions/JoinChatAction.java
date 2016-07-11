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

package org.symphonyoss.proxydesk.models.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.proxydesk.constants.HelpBotConstants;
import org.symphonyoss.proxydesk.models.users.DeskUser;
import org.symphonyoss.proxydesk.utils.ClientCache;
import org.symphonyoss.proxydesk.utils.DeskUserCache;
import org.symphonyoss.proxydesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/16/16.
 * An AiAction that allows a member to join a web.
 */
public class JoinChatAction implements AiAction {
    private final Logger logger = LoggerFactory.getLogger(JoinChatAction.class);
    private SymphonyClient symClient;

    public JoinChatAction(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    /**
     * Parse the message for email.
     * Find join desk user by email.
     * Join call.
     *
     * @param mlMessageParser the parser contains the input in ML
     * @param message         the received message
     * @param command         the command that triggered this action
     * @return the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();

        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

        DeskUser requester = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        DeskUser join = null;

        try {

            User user = symClient.getUsersClient().getUserFromEmail(email);
            if (user != null)
                join = DeskUserCache.getDeskUser(user.getId().toString());

        } catch (Exception e) {
            logger.error("An error occurred when finding an email.", e);
        }


        if (join != null && join.isOnCall()) {

            if (requester.getUserType() == DeskUser.DeskUserType.HELP_CLIENT) {
                join.getCall().enter(ClientCache.retrieveClient(message));

            } else if (requester.getUserType() == DeskUser.DeskUserType.MEMBER) {
                join.getCall().enter(MemberCache.getMember(message));

            }

        } else if (join != null) {

            userIdList.add(message.getFromUserId());
            aiResponseSequence.addResponse(new AiResponse(HelpBotConstants.NOT_ON_CALL,
                    MessageSubmission.FormatEnum.TEXT,
                    userIdList));

        } else {

            userIdList.add(message.getFromUserId());
            aiResponseSequence.addResponse(new AiResponse(email + HelpBotConstants.NOT_FOUND,
                    MessageSubmission.FormatEnum.TEXT,
                    userIdList));

        }


        return aiResponseSequence;
    }


}
