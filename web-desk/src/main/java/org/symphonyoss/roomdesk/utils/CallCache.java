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

package org.symphonyoss.roomdesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.roomdesk.models.HelpBotSession;
import org.symphonyoss.roomdesk.models.calls.MultiChatCall;
import org.symphonyoss.roomdesk.models.calls.MultiChatHelpCall;
import org.symphonyoss.roomdesk.models.calls.MultiChatWebCall;
import org.symphonyoss.roomdesk.models.users.HelpClient;
import org.symphonyoss.roomdesk.models.users.Member;
import org.symphonyoss.roomdesk.models.users.WebClient;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCache {
    public static final ConcurrentLinkedQueue<MultiChatCall> ACTIVECALLS = new ConcurrentLinkedQueue<MultiChatCall>();
    private static final Logger logger = LoggerFactory.getLogger(CallCache.class);

    public static MultiChatCall newCall(Member member, HelpClient helpClient, HelpBotSession helpBotSession) {
        if (member == null
                || helpClient == null
                || helpBotSession == null) {

            if (logger != null)
                logger.error("Could not create new call. NullPointer!");

            return null;
        }

        MultiChatCall newCall = new MultiChatHelpCall(member, helpClient, helpBotSession);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    public static MultiChatCall newCall(Member member, WebClient webClient, HelpBotSession helpBotSession) {
        if (member == null
                || webClient == null
                || helpBotSession == null) {

            if (logger != null)
                logger.error("Could not create new call. NullPointer!");

            return null;
        }

        MultiChatCall newCall = new MultiChatWebCall(member, webClient, helpBotSession);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    public static void endCall(MultiChatCall call) {

        if (call == null)
            return;

        ACTIVECALLS.remove(call);
        call.endCall();

    }

    public static void removeCall(MultiChatCall call) {

        ACTIVECALLS.remove(call);

    }
}
