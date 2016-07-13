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

package org.symphonyoss.webservice.models.session;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.webservice.WebServiceConstants;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.callback.AsyncCallback;
import org.symphonyoss.webservice.models.callback.ErrorCallback;
import org.symphonyoss.webservice.models.data.SessionData;
import org.symphonyoss.webservice.models.web.WebMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * this Session proxies conversation between a the web user (on a web page via WS
 * requests), and an external source
 */
public class WebSession implements Session {

    private static Logger logger = LoggerFactory.getLogger(WebSession.class);

    private SessionData sessionData;
    private List<WebMessage> transcription = new ArrayList<>();

    private SockJSSocket sockJSSocket;
    private Executor executor = Executors.newFixedThreadPool(20);

    private Set<WebSessionListener> externalListeners = new HashSet<>();

    private WebSession(SockJSSocket sockJSSocket, MultiMap sessionData,
                       AsyncCallback<WebSession> readyCallback,
                       ErrorCallback errorCallback) {

        this.sessionData = new SessionData(sessionData.get("name"),
                sessionData.get("email"), sessionData.get("topic"));
        this.sockJSSocket = sockJSSocket;

        initWebSocket(sockJSSocket);

        CompletableFuture.runAsync(
                () -> createRoom(readyCallback, errorCallback), executor);

    }

    public static Session init(SockJSSocket sockJSSocket, MultiMap sessionData,
                               AsyncCallback<WebSession> readyCallback,
                               ErrorCallback errorCallback) {

        return new WebSession(sockJSSocket,
                sessionData, readyCallback,
                errorCallback);

    }

    private boolean createRoom(AsyncCallback<WebSession> readyCallback, ErrorCallback errorCallback) {
        try {
            // The session is ready now, tell the caller of "init"
            readyCallback.callback(this);

            // Send a welcome message to the WS
            sendMessageToWebService(new WebMessage(System.currentTimeMillis(),
                    WebServiceConstants.HB_USER_NAME, sessionData.getEmail(), WebServiceConstants.WELCOME_MSG));

            return true;

        } catch (Exception e) {
            errorCallback.onError("Unexpected Exception: " + e.getMessage(), e);
        }
        return false;
    }

    private void initWebSocket(SockJSSocket sockJSSocket) {
        sockJSSocket.handler(c -> {
            // TODO - exception handling

            WebMessage message = new WebMessage(System.currentTimeMillis(),
                    sessionData.getName(), sessionData.getEmail(), c.toString());

            // Echo back to web client
            CompletableFuture.runAsync(() -> sendMessageToWebService(message), executor);
            String smsg = "<b>" + sessionData.getName() + ": " + "</b>" + message;
            CompletableFuture.runAsync(() -> onMessageEvent(message), executor);
        });
    }

    private void onMessageEvent(WebMessage webMessage) {

        for (WebSessionListener webSessionListener : externalListeners)
            webSessionListener.onNewWSMessage(webMessage);

    }

    public void sendMessageToWebService(WebMessage webMessage) {

        transcription.add(webMessage);
        String encodePrettily = Json.encodePrettily(webMessage);
        logger.debug("Sending to WS Client: " + encodePrettily);
        Buffer b = Buffer.buffer(encodePrettily);
        sockJSSocket.write(b);
        logger.debug("Send complete");

    }

    @Override
    public void terminateSession() {

        sendMessageToWebService(new WebMessage(System.currentTimeMillis(),
                WebServiceConstants.HB_USER_NAME,
                sessionData.getEmail(),
                WebServiceConstants.SESSION_TERMINATED_MESSAGE));

        // TODO - clean up the room?
        sockJSSocket.close();

    }

    @Override
    public List<WebMessage> getTranscription() {
        return null;
    }

    @Override
    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public SessionType getSessionType() {
        return SessionType.WEB;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    public void registerListener(WebSessionListener webSessionListener) {

        externalListeners.add(webSessionListener);

    }

    public void removeListener(WebSessionListener webSessionListener) {

        if (externalListeners.contains(webSessionListener))
            externalListeners.remove(webSessionListener);

    }

}
