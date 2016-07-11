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

package org.symphonyoss.roomdesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.roomdesk.constants.HelpBotConstants;
import org.symphonyoss.roomdesk.listeners.command.CallCommandListener;
import org.symphonyoss.roomdesk.models.users.DeskUser;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/21/16.
 * A model that allows a call to relay a message to both parties (Member and Client).
 */
public class CallResponder {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    private CallCommandListener callCommandListener;
    private Call call;
    protected SymphonyClient symClient;

    public CallResponder(Call call, SymphonyClient symClient) {
        this.symClient = symClient;
        this.call = call;
    }

    /**
     * Notify a user that they have successfully connected to the room
     *
     * @param user   the desk user to send to
     */
    public void sendConnectedMessage(DeskUser user) {
        if(user == null){

            if(logger != null)
                logger.error("Cannot send null user connected message {}.", user);

            return;
        }

        Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.CONNECTED_TO_CALL
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

    }

    public void sendLeftCallMessage(Long userID) {
        if(userID == null){

            if(logger != null)
                logger.error("Cannot send null user left message {}.", userID);

            return;
        }

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                MessageSubmission.FormatEnum.TEXT, userID, symClient);
    }
}
