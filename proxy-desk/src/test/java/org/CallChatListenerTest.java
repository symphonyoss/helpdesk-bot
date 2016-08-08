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

package org;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.proxydesk.listeners.chat.CallChatListener;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.Stream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallChatListenerTest {
    static CallChatListener listener = mock(CallChatListener.class);

    @Test
    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(listener).onChatMessage(new Message());
        Mockito.doCallRealMethod().when(listener).onChatMessage(null);
        Message message = new Message();

        try {
            listener.onChatMessage(null);
        } catch (Exception e) {
            fail("On web message null test failed.");
        }

        try {
            listener.onChatMessage(message);
        } catch (Exception e) {
            fail("On web message empty message test failed.");
        }

        message.setStreamId("TEST STREAM");
        try {
            listener.onChatMessage(message);
        } catch (Exception e) {
            fail("On web message junk stream, empty message test failed.");
        }
    }

    @Test
    public void testListenOn() throws Exception {
        Mockito.doCallRealMethod().when(listener).listenOn(null);
        Mockito.doCallRealMethod().when(listener).listenOn(new Chat());

        try {
            listener.listenOn(null);
        } catch (Exception e) {
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            listener.listenOn(chat);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Listen on empty web test failed.");
        }

        chat.setStream(new Stream());
        try {
            listener.listenOn(chat);
        } catch (Exception e) {
            fail("Listen on junk stream, empty web test failed.");
        }
    }

    @Test
    public void testStopListening() throws Exception {
        Mockito.doCallRealMethod().when(listener).stopListening(null);
        Mockito.doCallRealMethod().when(listener).stopListening(new Chat());

        try {
            listener.stopListening(null);
        } catch (Exception e) {
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            listener.stopListening(chat);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Listen on empty web test failed.");
        }

        chat.setStream(new Stream());
        try {
            listener.stopListening(chat);
        } catch (Exception e) {
            fail("Listen on junk stream, empty web test failed.");
        }
    }
}