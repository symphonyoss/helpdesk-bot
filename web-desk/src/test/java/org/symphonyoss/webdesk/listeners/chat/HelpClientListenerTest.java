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

package org.symphonyoss.webdesk.listeners.chat;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class HelpClientListenerTest {
    static HelpClientListener helpClientListenerTest = mock(HelpClientListener.class);

    @Test
    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).onChatMessage(new Message());
        Mockito.doCallRealMethod().when(helpClientListenerTest).onChatMessage(null);

        try {
            helpClientListenerTest.onChatMessage(null);
        } catch (Exception e) {
            fail("On web message null test failed.");
        }

        Message message = new Message();
        try {
            helpClientListenerTest.onChatMessage(message);
        } catch (Exception e) {
            fail("On web message empty message test failed.");
        }

        message.setStreamId("TEST STREAM");
        try {
            helpClientListenerTest.onChatMessage(message);
        } catch (Exception e) {
            fail("On web message junk stream, empty message test failed.");
        }
    }

    @Test
    public void testListenOn() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).listenOn(null);
        Mockito.doCallRealMethod().when(helpClientListenerTest).listenOn(new Chat());

        try {
            helpClientListenerTest.listenOn(null);
        } catch (Exception e) {
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            helpClientListenerTest.listenOn(chat);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Listen on empty web test failed.");
        }

        chat.setStream(new Stream());
        try {
            helpClientListenerTest.listenOn(chat);
        } catch (Exception e) {
            fail("Listen on junk stream, empty web test failed.");
        }
    }

    @Test
    public void testStopListening() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).stopListening(null);
        Mockito.doCallRealMethod().when(helpClientListenerTest).stopListening(new Chat());

        try {
            helpClientListenerTest.stopListening(null);
        } catch (Exception e) {
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            helpClientListenerTest.stopListening(chat);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Listen on empty web test failed.");
        }

        chat.setStream(new Stream());
        try {
            helpClientListenerTest.stopListening(chat);
        } catch (Exception e) {
            fail("Listen on junk stream, empty web test failed.");
        }
    }
}