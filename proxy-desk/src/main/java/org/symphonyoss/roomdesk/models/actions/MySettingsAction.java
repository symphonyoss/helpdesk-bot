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
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.roomdesk.constants.HelpBotConstants;
import org.symphonyoss.roomdesk.models.users.Member;
import org.symphonyoss.roomdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.Arrays;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class MySettingsAction implements AiAction {

    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        Member member = MemberCache.getMember(message);
        userIdList.add(member.getUserID());

        aiResponseSequence.addResponse(new AiResponse(MLTypes.START_ML.toString() +
        MLTypes.START_BOLD + member.getEmail() + ": " + MLTypes.END_BOLD
                + MLTypes.BREAK + HelpBotConstants.SEE_HELP_LABEL + member.isSeeHelpRequests()
                + MLTypes.BREAK + HelpBotConstants.HIDE_IDENTITY_LABEL + member.isHideIdentity()
                + MLTypes.BREAK + HelpBotConstants.TAGS_LABEL + Arrays.toString(member.getTags().toArray())
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, userIdList));

        return aiResponseSequence;
    }



}
