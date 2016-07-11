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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/17/16.
 * A model that allows the member class to be serialized and written to file
 */
public class SerializableMember {
    private String email;
    private Long userID;
    private Set<String> tags = new LinkedHashSet<String>();

    private boolean seeCommands = true;
    private boolean hideIdentity;

    public SerializableMember(String email, Long userID, boolean seeCommands, boolean hideIdentity, Set<String> tags) {
        this.email = email;
        this.userID = userID;
        this.seeCommands = seeCommands;
        this.hideIdentity = hideIdentity;
        this.tags = tags;
    }

    /**
     * @return   convert back to member
     */
    public Member toMember() {
        return new Member(email, userID, seeCommands, hideIdentity, tags);
    }



}
