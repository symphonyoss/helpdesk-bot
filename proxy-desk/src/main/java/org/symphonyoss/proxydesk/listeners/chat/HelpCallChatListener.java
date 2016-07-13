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

package org.symphonyoss.proxydesk.listeners.chat;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.proxydesk.constants.HelpBotConstants;
import org.symphonyoss.proxydesk.listeners.command.CallCommandListener;
import org.symphonyoss.proxydesk.models.calls.HelpCall;
import org.symphonyoss.proxydesk.models.users.DeskUser;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.MemberCache;

/**
 * Created by nicktarsillo on 6/27/16.
 */
public class HelpCallChatListener extends CallChatListener {
    private HelpCall helpCall;

    public HelpCallChatListener(HelpCall call, CallCommandListener callCommandListener, SymphonyClient symClient) {
        super(call, callCommandListener, symClient);
        helpCall = call;
    }

    @Override
    /**
     * Override the construct relay method.
     * Construct the message based upon the membership of the user.
     */
    protected String constructRelayMessage(DeskUser deskUser, String text) {
        if (deskUser.getUserType() == DeskUser.DeskUserType.MEMBER) {

            Member member = MemberCache.getMember(deskUser.getUserID().toString());
            if (member.isHideIdentity()) {

                return MLTypes.START_BOLD.toString() + HelpBotConstants.MEMBER_LABEL
                        + (helpCall.getMembers().indexOf(member) + 1) + ": " + MLTypes.END_BOLD + text;

            } else {

                return MLTypes.START_BOLD.toString()
                        + deskUser.getEmail() + ": " + MLTypes.END_BOLD + text;

            }

        } else {
            return super.constructRelayMessage(deskUser, text);
        }
    }
}
