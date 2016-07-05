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

package org.symphonyoss.sapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.symphonyoss.HelpBotConfig;
import org.symphonyoss.symphony.agent.api.MessagesApi;
import org.symphonyoss.symphony.pod.api.RoomMembershipApi;
import org.symphonyoss.symphony.pod.api.SessionApi;
import org.symphonyoss.symphony.pod.api.StreamsApi;
import org.symphonyoss.symphony.pod.api.UsersApi;
import org.symphonyoss.symphony.pod.invoker.ApiClient;
import org.symphonyoss.symphony.pod.invoker.ApiException;
import org.symphonyoss.symphony.pod.model.ImmutableRoomAttributes;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageList;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.RoomAttributes;
import org.symphonyoss.symphony.pod.model.RoomCreate;
import org.symphonyoss.symphony.pod.model.RoomDetail;
import org.symphonyoss.symphony.pod.model.SessionInfo;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserId;

public class ServiceAPI {

    private static final int DEFAULT_MESSAGE_POLL_INTERVAL = 500;

    private static final int MESSAGE_OVERLAP_WINDOW = 5000;

    private static Logger logger = LoggerFactory.getLogger(ServiceAPI.class);

    private StreamsApi streamsApi;
    private String sessionToken;
    private RoomMembershipApi memberApi;
    private long symphonyUserId;
    private String kmSession;
    private MessagesApi messagesApi;

    private ExecutorService exectorService = Executors.newFixedThreadPool(10);
    private UsersApi userLookupApi;

    private SessionApi sessionApi;

    private long messagePollInterval;

    public ServiceAPI(String podurl, String agentUrl, HelpBotConfig config) {
        this.messagePollInterval = config.getMessagePollPeriod(DEFAULT_MESSAGE_POLL_INTERVAL);
        try {
            kmSession = getAuthToken(config.getKeyAuthUrl());
            sessionToken = getAuthToken(config.getSessionAuthUrl());
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve authentication session: " + e.getMessage(), e);
        }

        logger.info("Successfully Retrieved kmSession and sessionToken");

        try {
            initPodAPIs(config);
            initAgentApis(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            SessionInfo a = sessionApi.v1SessioninfoGet(sessionToken);
            this.symphonyUserId = a.getUserId();
            logger.info("Successfully Retrieved HelpBot id: " + this.symphonyUserId);

        } catch (ApiException e) {
            throw new RuntimeException("Unable to retrieve HelpBot user id: " + e.getMessage(), e);
        }
    }

    public long getSymphonyUserId() {
        return symphonyUserId;
    }

    private void initPodAPIs(HelpBotConfig config) {
        ApiClient apiClient = new ApiClient();
        if (config.isDebugSymphonyAPI()) {
            apiClient = apiClient.setDebugging(true);
        }
        apiClient.setBasePath(config.getPodUrl());
        this.streamsApi = new StreamsApi(apiClient);
        this.memberApi = new RoomMembershipApi(apiClient);
        this.sessionApi = new SessionApi(apiClient);
        this.userLookupApi = new UsersApi(apiClient);
    }

    private void initAgentApis(HelpBotConfig config) {
        org.symphonyoss.symphony.agent.invoker.ApiClient agentClient = new org.symphonyoss.symphony.agent.invoker.ApiClient();
        if (config.isDebugSymphonyAPI()) {
            agentClient = agentClient.setDebugging(true);
        }

        agentClient.setBasePath(config.getAgentUrl());
        this.messagesApi = new MessagesApi(agentClient);
    }

    /**
     * A Synchronous request to create a room. This room will be owned by the
     * HelpBot user.
     *
     * @param roomName The Room name to be created.
     * @return The Room id
     * @throws ApiException
     */
    public String createRoom(String roomName) throws ApiException {
        logger.info("Creating room: " + roomName);
        RoomAttributes roomAttributes = new RoomAttributes();
        roomAttributes.setDescription(roomName);
        roomAttributes.setMembersCanInvite(true);
        roomAttributes.setName(roomName);
        roomAttributes.setDiscoverable(false);

        ImmutableRoomAttributes immutableRoomAttributes = new ImmutableRoomAttributes();
        immutableRoomAttributes.setCopyProtected(false);
        // Anybody can join
        immutableRoomAttributes.setPublic(true);
        immutableRoomAttributes.setReadOnly(false);

        RoomCreate payload = new RoomCreate();
        payload.setRoomAttributes(roomAttributes);
        payload.setImmutableRoomAttributes(immutableRoomAttributes);

        RoomDetail room = streamsApi.v1RoomCreatePost(payload, sessionToken);
        String streamId = room.getRoomSystemInfo().getId();
        logger.info("Room Created: " + streamId);
        return streamId;
    }

    public void joinRoom(String roomId, StreamListener l) throws ApiException {
        logger.info("Join Room: " + roomId);
        UserId userId = new UserId();
        userId.setId(symphonyUserId);
        memberApi.v1RoomIdMembershipAddPost(roomId, userId, sessionToken);
        logger.info("Joined Room: " + roomId);
    }

    public void sendMessage(String streamId, String message) {
        logger.info("sending Message to Symphony:  " + streamId + " " + message);
        try {
            MessageSubmission msg = new MessageSubmission();
            msg.setMessage(message);
            msg.setFormat(MessageSubmission.FormatEnum.MESSAGEML);
            messagesApi.v1StreamSidMessageCreatePost(streamId, sessionToken, kmSession, msg);

            logger.info("sent Message to Symphony:  " + streamId + " " + message);

        } catch (org.symphonyoss.symphony.agent.invoker.ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Long lookupUserByEmail(String agentEmailAddress) throws ApiException {
        logger.info("Looking up user:   [" + agentEmailAddress + "]");

        //TO DO:  We need to verify local flag.. it might break external comms.
        User user = this.userLookupApi.v1UserGet(agentEmailAddress, sessionToken, true);
        if (user == null) {
            logger.info("Unable to find user id for:   [" + agentEmailAddress + "]");
            return null;
            // TODO report error somewhere...
        }
        long agentSymphonyUserId = user.getId();
        logger.info("Found user id:   [" + agentEmailAddress + "] = [" + agentSymphonyUserId + "]");
        return agentSymphonyUserId;
    }

    public void addMemberToRoom(String roomId, long symUserId, boolean owner) throws ApiException {

        UserId userId = new UserId();
        userId.setId(symUserId);
        logger.info("Adding Member  [" + symUserId + "]  to Room: " + roomId);

        memberApi.v1RoomIdMembershipAddPost(roomId, userId, sessionToken);
        if (owner) {
            memberApi.v1RoomIdMembershipPromoteOwnerPost(roomId, userId, sessionToken);
        }
    }

    public void removeStreamListener(String streamId, StreamListener roomListener) {
        // TODO
    }

    public void addStreamListener(String streamId, StreamListener streamListener) {
        // TODO - share threads, remove listeners!
        logger.info("Polling for messages on " + streamId);
        new Thread() {
            public void run() {
                // Start polling from 5 seconds back.
                long now = System.currentTimeMillis() - MESSAGE_OVERLAP_WINDOW;
                HashSet<String> ids = new HashSet<>();
                while (true) {

                    try {
                        MessageList msgs;
                        msgs = messagesApi.v1StreamSidMessageGet(streamId, now, sessionToken, kmSession, 0, 1000);

                        long lastReceivedTS = 0;
                        if (msgs == null) {
                            continue;
                        }
                        for (Message message : msgs) {
                            // We retain a list of previously processed message
                            // ID's because
                            // it is possible for messages to be come available
                            // on the query out of sequence.
                            String msgid = message.getId();
                            if (ids.contains(msgid)) {
                                // We have previously processed this message,
                                // skip it.
                                continue;
                            }
                            ids.add(msgid);
                            logger.debug(
                                    "Received new message for " + streamId + " " + msgid + " " + message.getMessage());
                            long l = Long.parseLong(message.getTimestamp());
                            lastReceivedTS = l;

                            now = lastReceivedTS - MESSAGE_OVERLAP_WINDOW; // Overlap
                            Long from = message.getFromUserId();
                            streamListener.onMessage(new SymphonyMessage(l, from, message.getMessage()));
                        }
                    } catch (org.symphonyoss.symphony.agent.invoker.ApiException e) {
                        // TODO - handle this better!
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(messagePollInterval);
                    } catch (InterruptedException e) {
                        // carry on
                    }
                }
            }
        }.start();
    }

    public static String getAuthToken(String kmauth) throws MalformedURLException, IOException, ProtocolException {
        URL url = new URL(kmauth);
        HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
        urlConn.setRequestMethod("POST");
        // logger.info(urlConn.getResponseCode());
        String content = IOUtils.toString(urlConn.getInputStream());
        // logger.info(content);
        ObjectMapper m = new ObjectMapper();
        Map map = m.readValue(content, Map.class);
        return (String) map.get("token");
    }

}
