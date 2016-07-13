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

package org.symphonyoss.proxydesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.proxydesk.config.ProxyBotConfig;
import org.symphonyoss.proxydesk.models.actions.ExitAction;
import org.symphonyoss.proxydesk.models.actions.HelpSummaryAction;
import org.symphonyoss.proxydesk.models.actions.RoomInfoAction;
import org.symphonyoss.proxydesk.models.calls.Call;
import org.symphonyoss.proxydesk.models.permissions.IsHelpCall;
import org.symphonyoss.proxydesk.models.permissions.IsMember;

import static org.symphonyoss.proxydesk.config.ProxyBotConfig.Config;

/**
 * Created by nicktarsillo on 6/20/16.
 * A extension of the org.org.symphonyoss.ai command listener.
 * Initializes the required commands, used inside a call.
 */
public class CallCommandListener extends AiCommandListener {
    private Call call;

    public CallCommandListener(SymphonyClient symClient, Call call) {
        super(symClient);
        this.call = call;
        init();
    }

    /**
     * Create and add the commands used in a basic call.
     */
    private void init() {
        AiCommand exit = new AiCommand(Config.getString(ProxyBotConfig.EXIT), 0);
        exit.addAction(new ExitAction(call));

        AiCommand sendInfo = new AiCommand(Config.getString(ProxyBotConfig.ROOM_INFO), 0);
        sendInfo.addAction(new RoomInfoAction());
        sendInfo.addPermission(new IsHelpCall());

        AiCommand sendSummary = new AiCommand(Config.getString(ProxyBotConfig.HELP_SUMMARY), 0);
        sendSummary.addAction(new HelpSummaryAction());
        sendSummary.addPermission(new IsMember());
        sendSummary.addPermission(new IsHelpCall());

        getActiveCommands().add(sendInfo);
        getActiveCommands().add(sendSummary);
        getActiveCommands().add(exit);
    }


}
