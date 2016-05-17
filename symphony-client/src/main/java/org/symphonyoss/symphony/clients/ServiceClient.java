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

package org.symphonyoss.symphony.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.model.SymAuth;
import org.symphonyoss.symphony.service.api.PresenceApi;
import org.symphonyoss.symphony.service.api.StreamsApi;
import org.symphonyoss.symphony.service.api.UserApi;
import org.symphonyoss.symphony.service.api.UsersApi;
import org.symphonyoss.symphony.service.invoker.ApiClient;
import org.symphonyoss.symphony.service.model.*;


/**
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class ServiceClient {
    private SymAuth symAuth;
    private String serviceUrl;
    private ApiClient apiClient;

    private Logger logger = LoggerFactory.getLogger(ServiceClient.class);


    public ServiceClient(SymAuth symAuth, String serviceUrl) {

        this.symAuth = symAuth;
        this.serviceUrl = serviceUrl;


        //Get Service client to query for userID.
        apiClient = org.symphonyoss.symphony.service.invoker.Configuration.getDefaultApiClient();
        apiClient.setBasePath(serviceUrl);

        apiClient.addDefaultHeader(symAuth.getSessionToken().getName(), symAuth.getSessionToken().getToken());
        apiClient.addDefaultHeader(symAuth.getKeyToken().getName(), symAuth.getKeyToken().getToken());

    }

    public User getUserFromEmail(String email) throws Exception {


        UsersApi usersApi = new UsersApi(apiClient);


        User user = usersApi.v1UserGet(email, symAuth.getSessionToken().getToken(), true);


        if (user != null) {

            logger.debug("Found User: {}:{}", user.getEmailAddress(), user.getId());
            return user;
        }

        logger.warn("Could not locate user: {}", email);
        return null;


    }

    public User getUserFromId(Long userId) throws Exception {

        UserApi userApi = new UserApi(apiClient);

        UserDetail userDetail = userApi.v1AdminUserUidGet( symAuth.getSessionToken().getToken(), userId);

        User user = new User();
        user.setId(userDetail.getUserSystemInfo().getId());
        user.setEmailAddress(userDetail.getUserAttributes().getEmailAddress());

        return user;
    }


        public Stream getStream(User user) throws Exception {


        StreamsApi streamsApi = new StreamsApi(apiClient);

        UserIdList userIdList = new UserIdList();
        userIdList.add(user.getId());
        Stream stream = streamsApi.v1ImCreatePost(userIdList, symAuth.getSessionToken().getToken());

        logger.debug("Stream ID for user: {}:{} ", user.getEmailAddress(), stream.getId());

        return stream;


    }


    public Stream getStreamFromEmail(String email) throws Exception {


        return getStream(getUserFromEmail(email));
    }


    public PresenceList getAllUserPresence() throws Exception {


        PresenceApi presenceApi = new PresenceApi(apiClient);


        return presenceApi.v1PresenceGet(symAuth.getSessionToken().getToken());


    }


}
