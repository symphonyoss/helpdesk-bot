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

/**
 * Created by nicktarsillo on 6/17/16.
 * A model that allows the member class to be serialized and written to file
 */
public class SerializableMember {
    private String email;
    private String alias;
    private Long userID;
    private boolean useAlias;

    public SerializableMember(String email, Long userID, String alias, boolean useAlias) {
        this.email = email;
        this.userID = userID;
        this.alias = alias;
        this.useAlias = useAlias;
    }

    /**
     * @return convert back to member
     */
    public Member toMember() {
        return new Member(email, userID, alias, useAlias);
    }


}
