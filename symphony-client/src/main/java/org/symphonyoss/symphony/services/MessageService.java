/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.symphony.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.SymphonyClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageList;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.model.Conversation;
import org.symphonyoss.symphony.service.invoker.ApiClient;
import org.symphonyoss.symphony.service.model.Stream;
import org.symphonyoss.symphony.service.model.User;

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class MessageService {

    private SymphonyClient symphonyClient;
    private ApiClient serviceClient;
    private org.symphonyoss.symphony.agent.invoker.ApiClient agentClient;
    private boolean LOGIN_STATUS = false;
    private final String NOT_LOGGED_IN_MESSAGE = "Currently not logged into Agent, please check certificates and tokens.";
    private Logger logger = LoggerFactory.getLogger(MessageService.class);


    public MessageService(SymphonyClient symphonyClient){
                this.symphonyClient = symphonyClient;
    }



    public void sendMessage(Conversation conv, MessageSubmission message) throws Exception {

        symphonyClient.getAgentClient().sendMessage(conv.getStream(),message);

    }


    public MessageList getMessagesFromStream(Stream stream, Long since, Integer offset, Integer maxMessages) throws Exception {

        return symphonyClient.getAgentClient().getMessagesFromStream(
                stream, since, offset, maxMessages);

    }


    public MessageList getMessagesFromUserId(long userId, Long since, Integer offset, Integer maxMessages) throws Exception {


        User user = new User();
        user.setId(userId);


        return getMessagesFromStream(
                symphonyClient.getServiceClient().getStream(user), since, offset, maxMessages);


    }


}
