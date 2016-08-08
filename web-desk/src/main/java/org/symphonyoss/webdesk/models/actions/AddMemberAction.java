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

package org.symphonyoss.webdesk.models.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;
import org.symphonyoss.webdesk.constants.WebDeskConstants;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.webdesk.models.HelpBotSession;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.utils.ClientCache;
import org.symphonyoss.webdesk.utils.HoldCache;
import org.symphonyoss.webdesk.utils.MemberCache;

/**
 * Created by nicktarsillo on 6/15/16.
 * An AiAction that allows a member to add a member to the member cache.
 */
public class AddMemberAction implements AiAction {
    private final Logger logger = LoggerFactory.getLogger(AddMemberAction.class);

    private HelpClientListener helpClientListener;
    private MemberCommandListener memberCommandListener;
    private SymphonyClient symClient;

    public AddMemberAction(HelpBotSession helpBotSession) {
        this.helpClientListener = helpBotSession.getHelpClientListener();
        this.memberCommandListener = helpBotSession.getMemberListener();
        this.symClient = helpBotSession.getSymphonyClient();
    }

    /**
     * Promotes a client to member, as commanded from another member.
     * Parse message for email.
     * Find client by email.
     * Create new member, remove old client.
     * Add to cache, write to file.
     *
     * @param mlMessageParser the parser contains the input in ML
     * @param message         the received message
     * @param command         the command that triggered this action
     * @return the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence responseList = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();
        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

        try {
            User user = symClient.getUsersClient().getUserFromEmail(email);

            if (user != null
                    && user.getId() != null
                    && !MemberCache.hasMember(user.getId().toString())) {


                if (ClientCache.hasClient(user.getId())) {

                    if (HoldCache.hasClient(ClientCache.retrieveClient(user)))
                        HoldCache.pickUpClient(ClientCache.retrieveClient(user));

                    ClientCache.removeClient(user);

                }

                Member member = new Member(email,
                        user.getId());
                MemberCache.addMember(member);

                userIdList.add(message.getFromUserId());
                responseList.addResponse(new AiResponse(WebDeskConstants.PROMOTED_USER + email
                        + WebDeskConstants.TO_MEMBER, MessageSubmission.FormatEnum.TEXT, userIdList));

                userIdList = new UserIdList();
                userIdList.add(user.getId());
                responseList.addResponse(new AiResponse(WebDeskConstants.PROMOTED,
                        MessageSubmission.FormatEnum.TEXT, userIdList));

                Chat chat = symClient.getChatService().getChatByStream(
                        symClient.getStreamsClient().getStream(userIdList).getId());

                if (chat != null) {
                    helpClientListener.stopListening(chat);
                    memberCommandListener.listenOn(chat);
                }
            } else {

                userIdList.add(message.getFromUserId());
                responseList.addResponse(new AiResponse(WebDeskConstants.PROMOTION_FAILED, MessageSubmission.FormatEnum.TEXT,
                        userIdList));

            }


        } catch (Exception e) {
            logger.error("An error occurred when finding an email.", e);
        }

        return responseList;
    }


}
