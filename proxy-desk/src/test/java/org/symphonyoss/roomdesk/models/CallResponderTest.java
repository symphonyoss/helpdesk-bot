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
import org.symphonyoss.roomdesk.models.calls.HelpCallResponder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallResponderTest {
    static HelpCallResponder callResponder = mock(HelpCallResponder.class);

    @Test
    public void testSendRoomInfo() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendRoomInfo(new Long(0));
        Mockito.doCallRealMethod().when(callResponder).sendRoomInfo(null);

        try{
            callResponder.sendRoomInfo(null);
        }catch(Exception e){
            fail("send room info failed");
        }

        try{
            callResponder.sendRoomInfo(new Long(0));
        }catch(Exception e){
            fail("send room info failed");
        }
    }

    @Test
    public void testSendHelpSummary() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendHelpSummary(null);

        try{
            callResponder.sendHelpSummary(null);
        }catch(Exception e){
            fail("send help summary failed");
        }
    }

    @Test
    public void testSendConnectedMessage() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendConnectedMessage(null);

        try{
            callResponder.sendConnectedMessage(null);
        }catch(Exception e){
            fail("send connected failed");
        }
    }

}