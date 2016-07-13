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

package org.symphonyoss.proxydesk.listeners.presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.MemberCache;
import org.symphonyoss.symphony.pod.model.UserPresence;

/**
 * Created by nicktarsillo on 6/15/16.
 * A presence listener that checks if a member is busy or not.
 * If the user is busy, stop sending help requests.
 */
public class MemberPresenceListener implements PresenceListener {
    private final Logger logger = LoggerFactory.getLogger(MemberPresenceListener.class);

    public void onUserPresence(UserPresence userPresence) {
        if (userPresence == null
                || userPresence.getUid() == null) {

            if (logger != null)
                logger.warn("Presence listener received null value from presence {}." +
                        " Ignored message.", userPresence);

            return;
        }

        if (MemberCache.MEMBERS.containsKey(userPresence.getUid().toString())) {

            Member member = MemberCache.getMember(userPresence.getUid().toString());

            if (userPresence.getCategory() == UserPresence.CategoryEnum.AVAILABLE) {
                member.setBusy(false);
            } else {
                member.setBusy(true);
            }

        }
    }


}
