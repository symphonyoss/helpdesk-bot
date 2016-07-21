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

package org.symphonyoss.roomdesk.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.symphonyoss.roomdesk.models.calls.MultiChatCall;

/**
 * Created by nicktarsillo on 6/16/16.
 * A interface for a general user of the bot
 */
public interface DeskUser {

    @JsonIgnore
    MultiChatCall getCall();

    void setCall(MultiChatCall call);

    Long getUserID();

    void setUserID(Long userID);

    String getEmail();

    void setEmail(String userID);

    boolean isOnCall();

    void setOnCall(boolean onCall);

    @JsonIgnore
    DeskUserType getUserType();

    enum DeskUserType {
        HELP_CLIENT, MEMBER
    }


}
