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

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webdesk.bots.WebDeskBot;
import org.symphonyoss.webdesk.config.WebBotConfig;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.models.users.SerializableMember;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class MemberCache {
    public static final Map<String, Member> MEMBERS = new HashMap<String, Member>();
    private static final Logger logger = LoggerFactory.getLogger(MemberCache.class);

    public static void loadMembers() {

        File[] files = new File(System.getProperty(WebBotConfig.FILES_JSON)).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String ext = FilenameUtils.getExtension(name);
                return ext.equalsIgnoreCase("json");
            }
        });

        Gson gson = new Gson();

        if (files != null) {


            for (File file : files) {

                try {

                    logger.debug(file.getName());
                    Member member = gson.fromJson(new FileReader(file), SerializableMember.class).toMember();
                    addMember(member);
                    logger.debug("Loaded member {}", member.getUserID());

                } catch (IOException e) {
                    logger.error("Could not load json {} ", file.getName(), e);
                }

            }


        }
    }

    public static void writeMember(Member member) {

        if (member == null)
            return;

        try {

            Gson gson = new Gson();
            FileWriter jsonFile = new FileWriter(System.getProperty("files.json") + member.getUserID() + ".json");
            String toJson = gson.toJson(member.toSerializable(), SerializableMember.class);

            jsonFile.write(toJson);

            jsonFile.flush();
            jsonFile.close();

        } catch (IOException e) {
            logger.error("Could not write file for hashtag {}", member.getEmail(), e);
        }

    }

    public static void removeMember(Member member) {
        new File(System.getProperty("files.json") + member.getUserID() + ".json").delete();

        DeskUserCache.removeUser(member);
    }

    public static void addMember(Member member) {
        MemberCache.writeMember(member);

        MemberCache.MEMBERS.put(member.getUserID().toString(), member);
        DeskUserCache.addUser(member);
    }

    public static Set<Member> getOnlineMembers() {
        Set<Member> members = new HashSet<Member>();
        for (Member member : MEMBERS.values()) {

            if (member.isOnline() && !member.isBusy()) {

                members.add(member);

            }
        }

        return members;
    }

    public static String listMembers() {
        String list = "";
        int index = 1;
        for (Member member : MEMBERS.values()) {

            list += ", " + member.getEmail();

            index++;
        }

        if (MEMBERS.size() > 0)
            return list.substring(1);
        else
            return list;
    }

    public static String listOnlineMembers() {
        String list = "";
        int index = 1;
        for (Member member : MEMBERS.values()) {

            if (member.isOnline() && !member.isBusy()) {

                if (!member.isUseAlias())
                    list += ", " + member.getEmail();
                else
                    list += ", " + member.getAlias();

            }

            index++;
        }

        if (list.length() > 0) {
            return list.substring(1);
        } else {
            return list;
        }
    }

    public static Member getMember(User user) {
        return MEMBERS.get(user.getId().toString());
    }

    public static Member getMember(Message message) {
        return MEMBERS.get(message.getFromUserId().toString());
    }

    public static Member getMember(String userID) {
        return MEMBERS.get(userID);
    }

    public static boolean hasMember(String userID) {
        return MEMBERS.containsKey(userID);
    }

    public static int size() {
        return MEMBERS.size();
    }
}