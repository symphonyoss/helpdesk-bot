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

package org.symphonyoss.webdesk.models.users;

import org.symphonyoss.webdesk.constants.WebDeskConstants;
import org.symphonyoss.webdesk.models.calls.MultiChatCall;
import org.symphonyoss.webdesk.utils.MemberCache;

/**
 * Created by nicktarsillo on 6/14/16.
 * A model that represents a member.
 */
public class Member implements DeskUser {
    private MultiChatCall call;
    private String email;
    private String alias;
    private Long userID;
    private boolean onCall;
    private boolean busy;
    private boolean online;
    private boolean useAlias;

    public Member(String email, Long userID) {
        setEmail(email);
        setUserID(userID);
        setAlias(WebDeskConstants.MEMBER_LABEL
                + " " + (MemberCache.size() + 1));
    }

    public Member(String email, Long userID, String alias, boolean useAlias) {
        setEmail(email);
        setUserID(userID);
        setAlias(alias);
        setUseAlias(useAlias);
    }

    /**
     * @return the type of user (Member)
     */
    public DeskUserType getUserType() {
        return DeskUserType.MEMBER;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return if the user is in a call
     */
    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    public boolean isBusy() {
        return busy;
    }

    /**
     * @param busy if the user is busy
     */
    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    /**
     * @return the call the user is currently in
     */
    public MultiChatCall getCall() {
        return call;
    }

    public void setCall(MultiChatCall call) {
        this.call = call;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public SerializableMember toSerializable() {
        return new SerializableMember(email, userID, alias, useAlias);
    }

    public boolean isUseAlias() {
        return useAlias;
    }

    public void setUseAlias(boolean useAlias) {
        this.useAlias = useAlias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        if (alias != ""
                || alias != null)
            this.alias = alias;
    }
}
