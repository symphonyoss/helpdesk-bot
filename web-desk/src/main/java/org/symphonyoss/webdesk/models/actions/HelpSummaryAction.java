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

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;
import org.symphonyoss.webdesk.models.calls.MultiChatHelpCall;
import org.symphonyoss.webdesk.models.users.DeskUser;
import org.symphonyoss.webdesk.utils.DeskUserCache;

/**
 * Created by nicktarsillo on 6/17/16.
 * A AiAction that returns back a summary of requested help from all the clients in a call.
 */
public class HelpSummaryAction implements AiAction {

    /**
     * Send back the summary of help requested from all the clients in the room.
     *
     * @param mlMessageParser the parser contains the input in ML
     * @param message         the received message
     * @param command         the command that triggered this action
     * @return the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        userIdList.add(message.getFromUserId());

        DeskUser deskUser = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        if (deskUser != null) {

            aiResponseSequence.addResponse(new AiResponse(((MultiChatHelpCall) deskUser.getCall()).getHelpSummary(),
                    MessageSubmission.FormatEnum.MESSAGEML, userIdList));

        } else {

            aiResponseSequence.addResponse(new AiResponse("ERROR: DESK USER NOT FOUND. PLEASE CONTACT AND ADMIN.",
                    MessageSubmission.FormatEnum.TEXT, userIdList));

        }

        return aiResponseSequence;
    }


}
