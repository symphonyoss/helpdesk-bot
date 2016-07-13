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

import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webdesk.models.users.DeskUser;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class DeskUserCache {
    public static final ConcurrentHashMap<String, DeskUser> ALL_USERS = new ConcurrentHashMap<String, DeskUser>();

    public static void addUser(DeskUser user) {
        ALL_USERS.put(user.getUserID().toString(), user);
    }

    public static void removeUser(DeskUser user) {
        ALL_USERS.remove(user.getUserID().toString());
    }

    public static DeskUser getDeskUser(String userID) {
        if (userID != null) {
            return ALL_USERS.get(userID);
        } else {
            return null;
        }
    }

    public static boolean hasUser(User user) {
        return ALL_USERS.containsKey(user.getId());
    }
}
