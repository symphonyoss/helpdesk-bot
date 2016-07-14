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
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCache {
    public static final ArrayList<Call> ACTIVECALLS = new ArrayList<Call>();
    private static final Logger logger = LoggerFactory.getLogger(CallCache.class);

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

        ACTIVECALLS.remove(call);
        call.exitCall();

    }

    public static void removeCall(Call call) {

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

    public static String listQueue() {
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
        return ACTIVECALLS.indexOf(call) + 1;
    }
}
