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

package org.symphonyoss.webdesk.constants;


import org.symphonyoss.ai.constants.MLTypes;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class WebDeskConstants {
    //Text
    public static final String CALL_NOTIFY = " entered a call with ";
    public static final String CONNECTED_TO_CALL = "Connected to call.";
    public static final String MEMBER_LABEL = "Member ";
    public static final String EXIT_CALL = "Call has ended.";
    public static final String NOT_FOUND = " does not exist, or has not requested help.";
    public static final String NO_USERS = "There are no users that need help currently.";
    public static final String PROMOTION_FAILED = "Failed to promote client to member. Either client does not exist " +
            "or client is already a member.";
    public static final String PROMOTED = "You have been promoted to member!";
    public static final String PROMOTED_USER = "You have promoted ";
    public static final String TO_MEMBER = " to member!";
    public static final String CLIENT_QUEUE_LABEL = MLTypes.START_BOLD + "Client Queue: " + MLTypes.END_BOLD;
    public static final String MEMBERS_ONLINE = MLTypes.START_BOLD + "Online members: " + MLTypes.END_BOLD;
    public static final String COULD_NOT_LOCATE = "Could not locate symphony user profile.";
    public static final String OPENED_SESSION = " has opened a new web help session. ";
    public static final String TOPIC = " Topic: ";
    public static final String CALL_CACHE_LABEL = MLTypes.START_BOLD + "Call Cache: " + MLTypes.END_BOLD;
}
