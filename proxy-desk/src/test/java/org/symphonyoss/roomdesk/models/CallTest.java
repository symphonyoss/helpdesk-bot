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

package org.symphonyoss.roomdesk.models;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.proxydesk.models.calls.Call;
import org.symphonyoss.proxydesk.models.users.HelpClient;
import org.symphonyoss.proxydesk.models.users.Member;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallTest {
    static Call call = mock(Call.class);

    @Test
    public void testInitiateCall() throws Exception {
        Mockito.doCallRealMethod().when(call).initiateCall();

        try {
            call.initiateCall();
        } catch (Exception e) {
            fail("init failed");
        }
    }

    @Test
    public void testEnter() throws Exception {
        Mockito.doCallRealMethod().when(call).enter(new HelpClient(new String(), new Long(0)));

        try {
            call.enter(new HelpClient(new String(), new Long(0)));
        } catch (Exception e) {
            fail("enter failed");
        }
    }

    @Test
    public void testEnter1() throws Exception {
        Mockito.doCallRealMethod().when(call).enter(new Member(new String(), new Long(0)));

        try {
            call.enter(new Member(new String(), new Long(0)));
        } catch (Exception e) {
            fail("enter failed");
        }
    }

    @Test
    public void testExitCall() throws Exception {
        Mockito.doCallRealMethod().when(call).exitCall();

        try {
            call.exitCall();
        } catch (Exception e) {
            fail("exit failed");
        }
    }
}