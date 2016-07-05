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

package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.config.HelpBotConfig;
import org.symphonyoss.helpdesk.models.HelpBotSession;
import org.symphonyoss.helpdesk.models.actions.*;
import org.symphonyoss.helpdesk.models.calls.Call;
import org.symphonyoss.helpdesk.models.permissions.IsMember;
import org.symphonyoss.helpdesk.models.permissions.OffCall;
import org.symphonyoss.helpdesk.utils.CallCache;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.symphony.pod.model.User;

import static org.symphonyoss.helpdesk.config.HelpBotConfig.Config;

/**
 * Created by nicktarsillo on 6/20/16.
 * A extension of the org.symphonyoss.ai command listener.
 * Initializes all commands a member can command the org.symphonyoss.ai.
 */
public class MemberCommandListener extends AiCommandListener {
    private HelpBotSession helpBotSession;

    public MemberCommandListener(HelpBotSession helpBotSession) {
        super(helpBotSession.getSymphonyClient());
        helpBotSession.setMemberListener(this);
        this.helpBotSession = helpBotSession;
        init();
    }

    public void init() {

        AiCommand acceptNextHelpClient = new AiCommand(Config.getString(HelpBotConfig.ACCEPT_NEXT), 0);
        acceptNextHelpClient.addAction(new AcceptHelpAction(helpBotSession));
        acceptNextHelpClient.addPermission(new IsMember());
        acceptNextHelpClient.addPermission(new OffCall());

        AiCommand acceptHelpClient = new AiCommand(Config.getString(HelpBotConfig.ACCEPT), 1);
        acceptHelpClient.setArgument(0, "Client");
        acceptHelpClient.setPrefixRequirement(0, "@");
        acceptHelpClient.addAction(new AcceptHelpAction(helpBotSession));
        acceptHelpClient.addPermission(new IsMember());
        acceptHelpClient.addPermission(new OffCall());

        AiCommand addMember = new AiCommand(Config.getString(HelpBotConfig.ADD_MEMBER), 1);
        addMember.setArgument(0, "Client");
        addMember.setPrefixRequirement(0, "@");
        addMember.addAction(new AddMemberAction(helpBotSession));
        addMember.addPermission(new IsMember());
        addMember.addPermission(new OffCall());

        AiCommand onlineMembers = new AiCommand(Config.getString(HelpBotConfig.ONLINE_MEMBERS), 0);
        onlineMembers.addAction(new OnlineMembersAction());

        AiCommand queueResponse = new AiCommand(Config.getString(HelpBotConfig.CLIENT_QUEUE), 0);
        queueResponse.addAction(new ClientQueueAction());
        queueResponse.addPermission(new IsMember());
        queueResponse.addPermission(new OffCall());

        getActiveCommands().add(acceptNextHelpClient);
        getActiveCommands().add(acceptHelpClient);
        getActiveCommands().add(addMember);
        getActiveCommands().add(onlineMembers);
        getActiveCommands().add(queueResponse);

        setPushCommands(true);
    }

}
