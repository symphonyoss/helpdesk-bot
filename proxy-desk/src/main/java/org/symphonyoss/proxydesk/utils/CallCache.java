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

package org.symphonyoss.proxydesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.proxydesk.constants.HelpBotConstants;
import org.symphonyoss.proxydesk.models.HelpBotSession;
import org.symphonyoss.proxydesk.models.calls.Call;
import org.symphonyoss.proxydesk.models.calls.HelpCall;
import org.symphonyoss.proxydesk.models.users.DeskUser;
import org.symphonyoss.proxydesk.models.users.HelpClient;
import org.symphonyoss.proxydesk.models.users.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCache {
    public static final ArrayList<Call> ACTIVECALLS = new ArrayList<Call>();
    public static final Set<Double> ALL_CALL_TIMES = new HashSet<Double>();

    private static final Logger logger = LoggerFactory.getLogger(CallCache.class);
    private static double meanCallTime;

    /**
     * Starts a new help call.
     * @param member the member to start the call with
     * @param helpClient the client to start the call with
     * @param helpBotSession the helpbot session
     * @return the new call
     */
    public static Call newCall(Member member, HelpClient helpClient, HelpBotSession helpBotSession) {
        if (member == null
                || helpClient == null
                || helpBotSession == null) {

            if (logger != null)
                logger.error("Could not create new call. NullPointer!");

            return null;
        }

        Call newCall = new HelpCall(member, helpClient, helpBotSession);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    /**
     * Starts a new basic call.
     * @param symClient the symclient
     * @param isPrivate
     * @return the new call
     */
    public static Call newCall(SymphonyClient symClient, boolean isPrivate) {
        if (symClient == null) {

            if (logger != null)
                logger.error("Could not create new call. NullPointer!");

            return null;
        }

        Call newCall = new Call(symClient, isPrivate);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    public static void endCall(Call call) {

        if (call == null)
            return;

       removeCall(call);
        call.exitCall();

    }

    public static void removeCall(Call call) {


        if(call == null || call.getTimer() == null)
            return;

        ALL_CALL_TIMES.add(call.getTimer().getTime());

        ACTIVECALLS.remove(call);

    }

    public static void checkCallInactivity(int milliSeconds) {


        for (Call call : new LinkedList<Call>(ACTIVECALLS)) {
            call.setInactivityTime(call.getInactivityTime() + milliSeconds);

            if (call.getInactivityTime() > HelpBotConstants.MAX_INACTIVITY) {
                endCall(call);
                logger.debug("Removed call due to inactivity.");
            }

        }


    }

    public static String listCache() {
        String text = "";

        for(Call call : ACTIVECALLS) {
            text += ", " + call.toString();
        }

        if(text.length() != 0) {
            return text.substring(1);
        }else {
            return text;
        }

    }

    public static int size() {
        return ACTIVECALLS.size();
    }

    public static int getCallID(Call call) {
        return ACTIVECALLS.indexOf(call);
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
