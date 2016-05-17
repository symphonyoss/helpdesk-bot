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

package org.symphonyoss.symphony.clients;/**
 * Created by Frank Tarsillo on 5/15/2016.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.SymphonyClient;
import org.symphonyoss.symphony.model.SymAuth;
import org.symphonyoss.symphony.service.model.User;
import org.symphonyoss.symphony.services.ConversationService;
import org.symphonyoss.symphony.services.MessageService;
import org.symphonyoss.symphony.services.PresenceService;

public class SymphonyBasicClient implements SymphonyClient {

    private boolean LOGIN_STATUS = false;
    private final String NOT_LOGGED_IN_MESSAGE = "Currently not logged into Agent, please check certificates and tokens.";
    private Logger logger = LoggerFactory.getLogger(SymphonyBasicClient.class);
    private SymAuth symAuth;
    private MessageService messageService;
    private PresenceService presenceService;
    private AgentClient agentClient;
    private ServiceClient serviceClient;
    private ConversationService conversationService;
    private User localUser;



    public SymphonyBasicClient() {

    }


    public boolean init(SymAuth symAuth, String email, String agentUrl, String serviceUrl) throws Exception {

        if (symAuth == null || symAuth.getSessionToken() == null || symAuth.getKeyToken() == null)
            throw new Exception("Symphony Authorization is not valid", new Throwable(NOT_LOGGED_IN_MESSAGE));

        if (agentUrl == null)
            throw new Exception("Failed to provide agent URL", new Throwable("Failed to provide agent URL"));

        agentClient = new AgentClient(symAuth, agentUrl);

        if (serviceUrl == null)
            throw new Exception("Failed to provide service URL", new Throwable("Failed to provide service URL"));

        serviceClient = new ServiceClient(symAuth, serviceUrl);

        messageService = new MessageService(this);
        presenceService = new PresenceService(this);
        conversationService = new ConversationService(this);

        localUser = getServiceClient().getUserFromEmail(email);

        return true;
    }

    public SymAuth getSymAuth() {
        return symAuth;
    }

    public void setSymAuth(SymAuth symAuth) {
        this.symAuth = symAuth;
    }


    public AgentClient getAgentClient() {
        return agentClient;
    }

    public void setAgentClient(AgentClient agentClient) {
        this.agentClient = agentClient;
    }

    public ServiceClient getServiceClient() {
        return serviceClient;
    }

    public void setServiceClient(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }


    public MessageService getMessageService() {
        return messageService;
    }

    public PresenceService getPresenceService() {
        return presenceService;
    }


    public User getLocalUser() {
        return localUser;
    }

    public void setLocalUser(User localUser) {
        this.localUser = localUser;
    }

    public ConversationService getConversationService() {
        return conversationService;
    }

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }
}


