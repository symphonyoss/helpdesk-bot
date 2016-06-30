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

package org.symphonyoss.helpdesk.constants;


import org.symphonyoss.ai.constants.MLTypes;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpBotConstants {
    public static final int INACTIVITY_INTERVAL = 60000;
    public static final int MAX_INACTIVITY = INACTIVITY_INTERVAL * 3;
    public static final double CORRECTFACTOR = 0.6;

    //Text
    public static final String CONNECTED_TO_CALL = "Connected to call.";
    public static final String CLIENTS_LABEL = MLTypes.BREAK.toString() + MLTypes.START_BOLD + "Clients in room: " + MLTypes.END_BOLD;
    public static final String MEMBERS_LABEL = MLTypes.BREAK.toString() + MLTypes.START_BOLD + "Members in room: " + MLTypes.END_BOLD;
    public static final String HELP_SUMMARY_LABEL = MLTypes.BREAK.toString() + MLTypes.START_BOLD + "Help Summary: " + MLTypes.END_BOLD + MLTypes.BREAK;
    public static final String HELP_CLIENT_LABEL = "Help client ";
    public static final String ENTERED_CHAT = " has entered the chat.";
    public static final String MEMBER_LABEL = "Member ";
    public static final String EXIT_CALL = "You have exited the call.";
    public static final String LEFT_CALL = " has left the call.";
    public static final String NOT_FOUND = " does not exist, or has not requested help.";
    public static final String NO_USERS = "There are no users that need help currently.";
    public static final String PROMOTION_FAILED = "Failed to promote client to member. Either client does not exist " +
            "or client is already a member.";
    public static final String PROMOTED = "You have been promoted to member!";
    public static final String PROMOTED_USER = "You have promoted ";
    public static final String TO_MEMBER = " to member!";
    public static final String CLIENT_QUEUE_LABEL = MLTypes.START_BOLD + "Client Queue: " + MLTypes.END_BOLD;
    public static final String NOT_ON_CALL = "The specified user is not in a call.";
    public static final String MEMBERS_ONLINE = MLTypes.START_BOLD + "Online members: " + MLTypes.END_BOLD;
    public static final String SHOW_IDENTITY = "Your identity will now be shown.";
    public static final String HIDE_IDENTITY = "Your identity will now be hidden.";
    public static final String SEE_HELP = "You will now see help request messages.";
    public static final String HIDE_HELP = "You will no longer see help requests.";
    public static final String SEE_HELP_LABEL = "   See Help: ";
    public static final String HIDE_IDENTITY_LABEL = "   Hide Identity: ";
    public static final String TAGS_LABEL = "   Tags: ";
}
