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

package org.symphonyoss.webdesk.models;

import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webdesk.listeners.chat.TranscriptListener;
import org.symphonyoss.webdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.webservice.listeners.WebSessionListener;

/**
 * Created by nicktarsillo on 6/23/16.
 */
public class HelpBotSession {
    private SymphonyClient symphonyClient;
    private HelpClientListener helpClientListener;
    private MemberCommandListener memberListener;
    private WebSessionListener helpSession;
    private TranscriptListener transcriptListener;

    public HelpBotSession() {
    }

    public HelpBotSession(SymphonyClient symphonyClient, HelpClientListener helpClientListener, MemberCommandListener memberCommandListener) {
        this.symphonyClient = symphonyClient;
        this.helpClientListener = helpClientListener;
        this.memberListener = memberCommandListener;
    }

    public SymphonyClient getSymphonyClient() {
        return symphonyClient;
    }

    public void setSymphonyClient(SymphonyClient symphonyClient) {
        this.symphonyClient = symphonyClient;
    }

    public HelpClientListener getHelpClientListener() {
        return helpClientListener;
    }

    public void setHelpClientListener(HelpClientListener helpClientListener) {
        this.helpClientListener = helpClientListener;
    }

    public MemberCommandListener getMemberListener() {
        return memberListener;
    }

    public void setMemberListener(MemberCommandListener memberListener) {
        this.memberListener = memberListener;
    }

    public TranscriptListener getTranscriptListener() {
        return transcriptListener;
    }

    public void setTranscriptListener(TranscriptListener transcriptListener) {
        this.transcriptListener = transcriptListener;
    }

    public WebSessionListener getHelpSession() {
        return helpSession;
    }

    public void setHelpSession(WebSessionListener helpSession) {
        this.helpSession = helpSession;
    }
}
