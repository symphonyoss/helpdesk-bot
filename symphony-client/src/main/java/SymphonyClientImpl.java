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

/**
 * Created by Frank Tarsillo on 5/15/2016.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.agent.api.MessagesApi;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.authenticator.api.AuthenticationApi;
import org.symphonyoss.symphony.authenticator.invoker.Configuration;
import org.symphonyoss.symphony.authenticator.model.Token;
import org.symphonyoss.symphony.service.api.StreamsApi;
import org.symphonyoss.symphony.service.api.UsersApi;
import org.symphonyoss.symphony.service.invoker.ApiClient;
import org.symphonyoss.symphony.service.model.Stream;
import org.symphonyoss.symphony.service.model.User;
import org.symphonyoss.symphony.service.model.UserIdList;

class SymphonyClientImpl implements SymphonyClient {

    private Token sessionToken;
    private Token keyToken;
    private ApiClient serviceClient;
    private org.symphonyoss.symphony.agent.invoker.ApiClient agentClient;
    private boolean LOGIN_STATUS;
    private Logger logger = LoggerFactory.getLogger(SymphonyClientImpl.class);


    public SymphonyClientImpl() {


    }


    public static void main(String[] args) {

        SymphonyClient aClient = new SymphonyClientImpl();

        aClient.setKeystores("/dev/certs/server.truststore", System.getProperty("keystore.password"),
                "/dev/certs/bot.user1.p12", System.getProperty("keystore.password"));
        aClient.login("https://localhost.symphony.com:8444/sessionauth", "https://localhost.symphony.com:8444/keyauth",
                "https://localhost:8446/pod", "https://localhost:8446/agent");


        MessageSubmission message = new MessageSubmission();
        message.setMessage("THIS IS A MESSAGE FROM YOUR TEST CLIENT");
        message.setFormat(MessageSubmission.FormatEnum.TEXT);

        aClient.sendMessage(aClient.getStreamFromEmail("frank.tarsillo@markit.com"), message);


    }

    public void setKeystores(String serverTrustore, String truststorePass, String clientKeystore, String keystorePass) {


        System.setProperty("javax.net.ssl.trustStore", serverTrustore);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePass);
        System.setProperty("javax.net.ssl.keyStore", clientKeystore);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePass);
        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");


    }

    public boolean login(String authUrl, String keyUrl, String serviceUrl, String agentUrl) {

        try {
            org.symphonyoss.symphony.authenticator.invoker.ApiClient authenticatorClient = Configuration.getDefaultApiClient();

            // Configure the authenticator connection
            authenticatorClient.setBasePath(authUrl);

            // Get the authentication API
            AuthenticationApi authenticationApi = new AuthenticationApi(authenticatorClient);


            sessionToken = authenticationApi.v1AuthenticatePost();
            logger.debug("SessionToken: {} : {}", sessionToken.getName(), sessionToken.getToken());


            // Configure the keyManager path
            authenticatorClient.setBasePath(keyUrl);


            keyToken = authenticationApi.v1AuthenticatePost();
            logger.debug("KeyToken: {} : {}", keyToken.getName(), keyToken.getToken());


            //Get Service client to query for userID.
            serviceClient = org.symphonyoss.symphony.service.invoker.Configuration.getDefaultApiClient();
            serviceClient.setBasePath(serviceUrl);

            agentClient = org.symphonyoss.symphony.agent.invoker.Configuration.getDefaultApiClient();
            agentClient.setBasePath("https://localhost:8446/agent");


        } catch (Exception e) {
            System.err.println("Exception ");
            e.printStackTrace();
            return false;
        }

        LOGIN_STATUS = true;
        return true;
    }

    private User getUserFromEmail(String email) {

        if (!LOGIN_STATUS)
            return null;


        try {
            serviceClient.addDefaultHeader(sessionToken.getName(), sessionToken.getToken());
            serviceClient.addDefaultHeader(keyToken.getName(), keyToken.getToken());


            UsersApi usersApi = new UsersApi(serviceClient);


            User user = usersApi.v1UserGet(email, sessionToken.getToken(), true);


            if (user != null) {

                logger.debug("Found User: {}:{}", user.getEmailAddress(), user.getId());
                return user;
            } else {

                logger.warn("Could not locate user: {}", email);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return null;

    }

    public Stream getStream(User user) {

        try {


            StreamsApi streamsApi = new StreamsApi(serviceClient);

            UserIdList userIdList = new UserIdList();
            userIdList.add(user.getId());
            Stream stream = streamsApi.v1ImCreatePost(userIdList, sessionToken.getToken());

            logger.debug("Stream ID for user: {}:{} ", user.getEmailAddress(), stream.getId());

            return stream;


        } catch (Exception e) {
            System.err.println("Exception ");
            e.printStackTrace();
        }

        return null;
    }

    public Token getSessionToken() {
        return sessionToken;
    }

    public Token getKeyToken() {
        return keyToken;
    }

    public Stream getStreamFromEmail(String email) {


        return getStream(getUserFromEmail(email));
    }

    public Message sendMessage(Stream stream, MessageSubmission message) {

        try {
            MessagesApi messagesApi = new MessagesApi(agentClient);


            return messagesApi.v1StreamSidMessageCreatePost(stream.getId(), sessionToken.getToken(), keyToken.getToken(), message);
        } catch (org.symphonyoss.symphony.agent.invoker.ApiException e) {
            e.printStackTrace();
        }

        return null;
    }
}


