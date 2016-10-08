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
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.proxydesk.constants.HelpBotConstants;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.MemberCache;
import org.symphonyoss.symphony.clients.model.SymMessage;

import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/15/16.
 * An AiAction that allows a member to toggle the visibility of his alias.
 */
public class ToggleAliasAction implements AiAction {
    private final Logger logger = LoggerFactory.getLogger(ToggleAliasAction.class);

    /**
     * Find member by message from id.
     * Set identity to the opposite of the SymUser's current identity preference.
     * Write member.
     *
     * @param mlMessageParser the parser contains the input in ML
     * @param message         the received message
     * @param command         the command that triggered this action
     * @return the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, SymMessage message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();

        Member member = MemberCache.getMember(message);
        if (member != null) {

            member.setHideIdentity(!member.isHideIdentity());
            if (member.isHideIdentity()) {

                userIdList.add(message.getFromUserId());
                aiResponseSequence.addResponse(new AiResponse(HelpBotConstants.HIDE_IDENTITY,
                        SymMessage.Format.TEXT, userIdList));

            } else {

                userIdList.add(message.getFromUserId());
                aiResponseSequence.addResponse(new AiResponse(HelpBotConstants.SHOW_IDENTITY,
                        SymMessage.Format.TEXT, userIdList));

            }

        } else {
            logger.warn("Member {} could not be found. Ignoring action.", message.getFromUserId());
        }

        MemberCache.writeMember(member);

        return aiResponseSequence;
    }


}
