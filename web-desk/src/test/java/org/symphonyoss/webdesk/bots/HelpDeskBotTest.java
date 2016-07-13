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

package org.symphonyoss.webdesk.bots;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webdesk.bots.WebDeskBot;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/21/16.
 */
public class HelpDeskBotTest {

    @Test
    public void testSetupBot() throws Exception {
        WebDeskBot bot = mock(WebDeskBot.class);
        Mockito.doCallRealMethod().when(bot).setupBot();
        try {
            bot.setupBot();
        } catch (Exception e) {
            fail("Setup failed.");
        }
    }

    @Test
    public void testInitConnection() throws Exception {
        WebDeskBot bot = mock(WebDeskBot.class);
        Mockito.doCallRealMethod().when(bot).initConnection();
        try {
            bot.initConnection();
        } catch (Exception e) {
            fail("init failed.");
        }
    }

    @Test
    public void testOnNewChat() {
        WebDeskBot bot = mock(WebDeskBot.class);
        Mockito.doCallRealMethod().when(bot).onNewChat(new Chat());

        Chat chat = new Chat();
        chat.setStream(new Stream());
        Set<User> users = new HashSet<User>();
        users.add(new User());
        chat.setRemoteUsers(users);
        try {
            bot.onNewChat(chat);
        } catch (Exception e) {
        }
    }

    @Test
    public void testOnRemovedChat() {
        WebDeskBot bot = mock(WebDeskBot.class);
        try {
            bot.onRemovedChat(new Chat());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Remove failed.");
        }
    }
}