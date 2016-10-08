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

package org.symphonyoss.roomdesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.roomdesk.config.RoomBotConfig;
import org.symphonyoss.roomdesk.constants.HelpBotConstants;
import org.symphonyoss.roomdesk.models.HelpBotSession;
import org.symphonyoss.roomdesk.models.users.HelpClient;
import org.symphonyoss.roomdesk.models.users.Member;
import org.symphonyoss.roomdesk.utils.CallCache;
import org.symphonyoss.roomdesk.utils.HoldCache;
import org.symphonyoss.roomdesk.utils.MemberCache;
import org.symphonyoss.symphony.clients.model.SymMessage;

import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/14/16.
 * A AiAction that allows a member to accept a help client into a call.
 */
public class AcceptHelpAction implements AiAction {
    private HelpBotSession helpBotSession;

    public AcceptHelpAction(HelpBotSession helpBotSession) {
        this.helpBotSession = helpBotSession;
    }

    /**
     * Accept a client into a chat, where a member can assist.
     *
     * @param mlMessageParser the parser contains the input in ML
     * @param message         the received message
     * @param command         the command that triggered this action
     * @return the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, SymMessage message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList sendTo = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();

        Member member = MemberCache.getMember(message);

        if (command != null
                && chunks.length > command.getCommand().split(" ").length) {
            String email = String.join(" ", chunks);
            email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

            HelpClient helpClient = HoldCache.findClientCredentialMatch(email);

            if (helpClient != null) {
                CallCache.newCall(member, HoldCache.pickUpClient(helpClient), helpBotSession);

                Chat chat = helpBotSession.getSymphonyClient().getChatService().getChatByStream(System.getProperty(RoomBotConfig.MEMBER_CHAT_STREAM));

                member = MemberCache.getMember(message);
                String useName = member.getEmail();
                if (member.isUseAlias())
                    useName = member.getAlias();

                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + useName + MLTypes.END_BOLD
                                + HelpBotConstants.CALL_NOTIFY + MLTypes.START_BOLD
                                + helpClient.getEmail() + MLTypes.END_BOLD + MLTypes.END_ML,
                        SymMessage.Format.MESSAGEML, chat, helpBotSession.getSymphonyClient());

            } else {

                sendTo.add(message.getFromUserId());
                aiResponseSequence.addResponse(new AiResponse(email + HelpBotConstants.NOT_FOUND,
                        SymMessage.Format.TEXT, sendTo));

            }

        } else {

            if (HoldCache.ONHOLD.size() > 0) {
                HelpClient client = HoldCache.pickUpNextClient();

                CallCache.newCall(member, client, helpBotSession);

                Chat chat = helpBotSession.getSymphonyClient().getChatService().getChatByStream(System.getProperty(RoomBotConfig.MEMBER_CHAT_STREAM));

                member = MemberCache.getMember(message);
                String useName = member.getEmail();
                if (member.isUseAlias())
                    useName = member.getAlias();

                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + useName + MLTypes.END_BOLD
                                + HelpBotConstants.CALL_NOTIFY + MLTypes.START_BOLD
                                + client.getEmail() + MLTypes.END_BOLD + MLTypes.END_ML,
                        SymMessage.Format.MESSAGEML, chat, helpBotSession.getSymphonyClient());
            } else {

                sendTo.add(message.getFromUserId());
                aiResponseSequence.addResponse(new AiResponse(HelpBotConstants.NO_USERS,
                        SymMessage.Format.TEXT, sendTo));

            }

        }

        return aiResponseSequence;
    }


}
