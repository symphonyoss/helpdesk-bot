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

import org.symphonyoss.webdesk.models.users.HelpClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class HoldCache {
    public static final ArrayList<HelpClient> ONHOLD = new ArrayList<HelpClient>();
    public static final Set<Double> ALL_HOLD_TIMES = new HashSet<Double>();

    public static void putClientOnHold(HelpClient client) {

        client.getHoldTimer().start();

        ONHOLD.add(client);
    }

    public static HelpClient pickUpNextClient() {
        HelpClient client = ONHOLD.get(0);

        client.getHoldTimer().stop();
        ALL_HOLD_TIMES.add(client.getHoldTimer().getTime());
        client.getHoldTimer().setTime(0);

        ONHOLD.remove(client);
        return client;
    }

    public static HelpClient pickUpClient(HelpClient client) {
        ONHOLD.remove(client);

        client.getHoldTimer().stop();
        ALL_HOLD_TIMES.add(client.getHoldTimer().getTime());
        client.getHoldTimer().setTime(0);

        return client;
    }

    public static HelpClient findClientCredentialMatch(String credential) {
        for (HelpClient client : HoldCache.ONHOLD)
            if (credential.equalsIgnoreCase(client.getEmail()) || credential.equalsIgnoreCase(client.getUserID().toString())) {
                return client;
            }

        return null;
    }

    public static String listQueue() {
        String list = "";
        for (HelpClient client : ONHOLD)
            if (client.getEmail() != "" && client.getEmail() != null)
                list += ", " + client.getEmail();
            else
                list += ", " + client.getUserID();
        if (ONHOLD.size() > 0)
            return list.substring(1);
        else
            return list;
    }

    public static boolean hasClient(HelpClient client) {
        return ONHOLD.contains(client);
    }

    public static double getMeanHoldTime() {
        double meanTime = 0;

        for(Double time : ALL_HOLD_TIMES)
            meanTime += time;

        return meanTime / ALL_HOLD_TIMES.size();
    }

    public static double getMaxHoldTime(){
        double max = 0;

        for(Double time : ALL_HOLD_TIMES)
            if(time > max)
                max = time;

        return max;
    }

}
