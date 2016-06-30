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

package org.symphonyoss.helpdesk.listeners.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.models.calls.Call;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.pod.model.User;

import java.util.Set;

/**
 * Created by nicktarsillo on 6/21/16.
 * Handles removing or adding chats in a call.
 */
public class CallServiceListener implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(CallServiceListener.class);
    private Call call;

    public CallServiceListener(Call call) {
        this.call = call;
    }

    public void onNewChat(Chat chat) {
        //Not possible so do nothing
    }

    /**
     * On remove chat, exit remote user from call
     * @param chat   the removed chat
     */
    public void onRemovedChat(Chat chat) {


        if (chat != null) {
            Set<User> users = chat.getRemoteUsers();

            if (users != null && users.size() > 0) {
                User user = users.iterator().next();

                DeskUser deskUser = null;
                if(user.getId() != null)
                    deskUser = DeskUserCache.getDeskUser(user.getId().toString());

                if (deskUser != null) {

                    if (deskUser.getUserType() == DeskUser.DeskUserType.MEMBER) {
                        call.exit(MemberCache.getMember(deskUser.getUserID().toString()));
                    }else if (deskUser.getUserType() == DeskUser.DeskUserType.HELP_CLIENT) {
                        call.exit(ClientCache.retrieveClient(deskUser.getUserID().toString()));
                    }

                }else{
                    logger.warn("Could not find desk user {}." +
                            " Ignoring chat removal.", user.getId());
                }

            }
        }


    }



}
