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

import org.symphonyoss.proxydesk.models.users.HelpClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 6/16/16.
 * A cache that holds all the help clients
 */
public class ClientCache {
    public static final ConcurrentHashMap<String, HelpClient> ALL_CLIENTS = new ConcurrentHashMap<String, HelpClient>();

    public static HelpClient addClient(SymUser SymUser) {
        if (SymUser == null)
            return null;

        HelpClient helpClient = new HelpClient(SymUser.getEmailAddress(), SymUser.getId());

        ALL_CLIENTS.put(SymUser.getId().toString(),
                helpClient);
        DeskUserCache.addUser(helpClient);

        return helpClient;
    }

    public static HelpClient retrieveClient(SymMessage message) {
        return ALL_CLIENTS.get(message.getFromUserId().toString());
    }

    public static HelpClient removeClient(SymUser SymUser) {
        HelpClient client = retrieveClient(SymUser);
        ALL_CLIENTS.remove(SymUser.getId().toString());
        DeskUserCache.removeUser(client);

        return client;
    }

    public static HelpClient retrieveClient(SymUser SymUser) {
        return ALL_CLIENTS.get(SymUser.getId().toString());
    }

    public static HelpClient retrieveClient(String userID) {
        return ALL_CLIENTS.get(userID);
    }

    public static boolean hasClient(Long id) {
        if (id == null)
            return false;

        return ALL_CLIENTS.containsKey(id.toString());
    }

    public static int getTotalHelpMessages() {
        int total = 0;

        for(HelpClient client : ALL_CLIENTS.values())
            total += client.getHelpRequests().size();

        return total;
    }
}
