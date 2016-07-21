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
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.webdesk.models.calls.MultiChatCall;

/**
 * Created by nicktarsillo on 6/16/16.
 * An AiAction that allows a client or member to exit a call.
 */
public class ExitAction implements AiAction {
    private MultiChatCall call;

    public ExitAction(MultiChatCall call) {
        this.call = call;
    }

    /**
     * Find user by from message id.
     * Exit the call.
     *
     * @param mlMessageParser the parser contains the input in ML
     * @param message         the received message
     * @param command         the command that triggered this action
     * @return the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();

        call.endCall();

        return aiResponseSequence;
    }


}
