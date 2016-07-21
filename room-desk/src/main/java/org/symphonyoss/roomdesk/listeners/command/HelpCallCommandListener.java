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

package org.symphonyoss.roomdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.roomdesk.config.RoomBotConfig;
import org.symphonyoss.roomdesk.models.actions.ExitAction;
import org.symphonyoss.roomdesk.models.actions.HelpSummaryAction;
import org.symphonyoss.roomdesk.models.actions.RoomInfoAction;
import org.symphonyoss.roomdesk.models.calls.MultiChatCall;
import org.symphonyoss.roomdesk.models.permissions.IsHelpCall;
import org.symphonyoss.roomdesk.models.permissions.IsMember;

import static org.symphonyoss.roomdesk.config.RoomBotConfig.Config;

/**
 * Created by nicktarsillo on 6/20/16.
 * A extension of the ai command listener.
 * Initializes the required commands, used inside a call.
 */
public class HelpCallCommandListener extends AiCommandListener {
    private MultiChatCall call;

    public HelpCallCommandListener(SymphonyClient symClient, MultiChatCall call) {
        super(symClient);
        this.call = call;
        init();
    }

    private void init() {

        AiCommand sendInfo = new AiCommand(Config.getString(RoomBotConfig.ROOM_INFO), 0);
        sendInfo.addAction(new RoomInfoAction());
        sendInfo.addPermission(new IsHelpCall());

        AiCommand sendSummary = new AiCommand(Config.getString(RoomBotConfig.HELP_SUMMARY), 0);
        sendSummary.addAction(new HelpSummaryAction());
        sendSummary.addPermission(new IsMember());
        sendSummary.addPermission(new IsHelpCall());

        AiCommand exit = new AiCommand(Config.getString(RoomBotConfig.EXIT), 0);
        exit.addAction(new ExitAction(call));
        exit.addPermission(new IsHelpCall());

        getActiveCommands().add(sendInfo);
        getActiveCommands().add(sendSummary);
        getActiveCommands().add(exit);
    }


}
