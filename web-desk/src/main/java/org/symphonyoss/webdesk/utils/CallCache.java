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

package org.symphonyoss.webdesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.webdesk.models.HelpBotSession;
import org.symphonyoss.webdesk.models.calls.MultiChatCall;
import org.symphonyoss.webdesk.models.calls.MultiChatHelpCall;
import org.symphonyoss.webdesk.models.calls.MultiChatWebCall;
import org.symphonyoss.webdesk.models.users.HelpClient;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.models.users.WebClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCache {
    public static final ArrayList<MultiChatCall> ACTIVECALLS = new ArrayList<>();
    public static final Set<Double> ALL_CALL_TIMES = new HashSet<Double>();

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

        removeCall(call);
        call.endCall();

    }

    public static void removeCall(MultiChatCall call) {
        if(call == null)
            return;

        if(call.getCallType() == MultiChatCall.CallTypes.HELP_CALL)
            ALL_CALL_TIMES.add(((MultiChatHelpCall) call).getCallTimer().getTime());

        ACTIVECALLS.remove(call);

    }

    public static String listCache() {
        String text = "";

        for(MultiChatCall call : ACTIVECALLS) {
            text += ", " + call.toString();
        }

        if(text.length() != 0) {
            return text.substring(1);
        }else {
            return text;
        }

    }

    public static int getCallID(MultiChatHelpCall multiChatHelpCall) {
        return ACTIVECALLS.indexOf(multiChatHelpCall);
    }

    public static double getMeanCallTime() {
        double meanCallTime = 0;

        for(Double time : ALL_CALL_TIMES)
            meanCallTime += time;

        return meanCallTime / ALL_CALL_TIMES.size();
    }

    public static double maxCallTime(){
        double max = 0;

        for(Double time : ALL_CALL_TIMES)
            if(time > max)
                max = time;

        return max;
    }



}
