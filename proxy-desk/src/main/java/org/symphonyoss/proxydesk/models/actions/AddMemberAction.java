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
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.proxydesk.constants.HelpBotConstants;
import org.symphonyoss.proxydesk.listeners.chat.HelpClientListener;
import org.symphonyoss.proxydesk.listeners.command.MemberCommandListener;
import org.symphonyoss.proxydesk.models.HelpBotSession;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.ClientCache;
import org.symphonyoss.proxydesk.utils.MemberCache;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.UserIdList;

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
    public AiResponseSequence respond(MlMessageParser mlMessageParser, SymMessage message, AiCommand command) {
        AiResponseSequence responseList = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();
        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

        try {
            SymUser SymUser = symClient.getUsersClient().getUserFromEmail(email);

            if (SymUser != null
                    && SymUser.getId() != null
                    && !MemberCache.hasMember(SymUser.getId().toString())) {

                if (ClientCache.hasClient(SymUser.getId()))
                    ClientCache.removeClient(SymUser);

                Member member = new Member(email,
                        SymUser.getId());
                MemberCache.addMember(member);

                userIdList.add(message.getFromUserId());
                responseList.addResponse(new AiResponse(HelpBotConstants.PROMOTED_USER + email
                        + HelpBotConstants.TO_MEMBER, SymMessage.Format.TEXT, userIdList));

                userIdList = new UserIdList();
                userIdList.add(SymUser.getId());
                responseList.addResponse(new AiResponse(HelpBotConstants.PROMOTED,
                        SymMessage.Format.TEXT, userIdList));

                Chat chat = symClient.getChatService().getChatByStream(
                        symClient.getStreamsClient().getStream(userIdList).getId());

                if (chat != null) {
                    helpClientListener.stopListening(chat);
                    memberCommandListener.listenOn(chat);
                }
            } else {

                userIdList.add(message.getFromUserId());
                responseList.addResponse(new AiResponse(HelpBotConstants.PROMOTION_FAILED, SymMessage.Format.TEXT,
                        userIdList));

            }


        } catch (Exception e) {
            logger.error("An error occurred when finding an email.", e);
        }

        return responseList;
    }


}
