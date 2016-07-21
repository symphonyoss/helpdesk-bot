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

package org.symphonyoss.ai.models;

import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/20/16.
 * A model that represents a single response from the ai.
 */
public class AiResponse {
    private String message;
    private MessageSubmission.FormatEnum type;
    private UserIdList toIDs = new UserIdList();

    public AiResponse(String message, MessageSubmission.FormatEnum type, UserIdList userIdList) {
        this.type = type;
        this.message = message;
        this.toIDs = userIdList;
    }

    /**
     * @return the response message
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the format of the message
     */
    public MessageSubmission.FormatEnum getType() {
        return type;
    }

    public void setType(MessageSubmission.FormatEnum type) {
        this.type = type;
    }

    public UserIdList getToIDs() {
        return toIDs;
    }

    public void setToIDs(UserIdList toIDs) {
        this.toIDs = toIDs;
    }
}
