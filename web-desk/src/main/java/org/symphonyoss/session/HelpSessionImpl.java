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

package org.symphonyoss.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.symphonyoss.HelpBotConfig;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.sapi.AsyncCallback;
import org.symphonyoss.sapi.ErrorCallback;
import org.symphonyoss.sapi.StreamListener;
import org.symphonyoss.sapi.SymphonyMessage;
import org.symphonyoss.web.Message;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

/**
 * this HelpSession proxies conversation between a helpee (on a web page via WS
 * requests), and one or more agents (on Symphony)
 *
 */
public class HelpSessionImpl implements HelpSession {

	private static Logger logger = LoggerFactory.getLogger(HelpSessionImpl.class);

	private SymphonyClient symClient;

	private Map<Long, String> aliasMap = new HashMap<>();
	private String roomId;
	private MultiMap helpRequest;
	private String helpeeName;
	private List<Message> transcription = new ArrayList<>();

	private SockJSSocket sockJSSocket;
	private Executor executor = Executors.newFixedThreadPool(20);

	private String terminationString;

	public static HelpSession init(SockJSSocket sockJSSocket, MultiMap helpRequest, SymphonyClient symClient, String username,
								   AsyncCallback<HelpSession> readyCallback, ErrorCallback errorCallback) {
		return new HelpSessionImpl(sockJSSocket, helpRequest, symClient, username, readyCallback, errorCallback);
	}

	private HelpSessionImpl(SockJSSocket sockJSSocket, MultiMap helpRequest, SymphonyClient symClient, String username,
			AsyncCallback<HelpSession> readyCallback, ErrorCallback errorCallback) {
		this.helpRequest = helpRequest;
		this.helpeeName = username;
		this.symClient = symClient;
		this.sockJSSocket = sockJSSocket;
		this.terminationString = "/close";
		String roomName = HelpBotConfig.HB_ROOM_PREFIX + UUID.randomUUID().toString();

		initWebSocket(sockJSSocket);

		sendMessageToWS(HelpBotConfig.HB_USER_NAME, HelpBotConfig.PLEASE_WAIT);

		StreamListener symphonyMessageListener = (m) -> handleMessageFromSymphony(m);
		// Do this asynchronously
		CompletableFuture.runAsync(
				() -> createRoom(symClient, roomName, symphonyMessageListener, readyCallback, errorCallback), executor);
	}

	private void handleMessageFromSymphony(SymphonyMessage m) {
		if (m.getFrom() == symClient.getLocalUser().getId()) {
			// This message came from us, so ignore it!
			return;
		}

		String agentAlias = aliasMap.get(m.getFrom());
		if (agentAlias == null) {
			agentAlias = "Agent";
		} else {
			agentAlias = "Agent: " + agentAlias;
		}
		String message = removeMessageML(m);
		if (terminationString.equals(message.trim())) {
			terminateSession();
		} else {
			sendMessageToWS(agentAlias, message);
		}
	}

	private String removeMessageML(SymphonyMessage m) {
		return m.getMessage().replaceAll("</messageML>", "").replaceAll("<messageML>", "");
	}

	private boolean createRoom(SymphonyClient symClient, String roomName, final StreamListener symphonyMessageListener,
			AsyncCallback<HelpSession> readyCallback, ErrorCallback errorCallback) {
		try {
			// Save the Symphony Room Id
			roomId = symClient.createRoom(roomName);
			// "subscribe" to messages in this room

			// The session is ready now, tell the caller of "init"
			readyCallback.callback(HelpSessionImpl.this);
			// Send a welcome message to the WS
			sendMessageToWS(HelpBotConfig.HB_USER_NAME, HelpBotConfig.WELCOME_MSG);
			return true;

		} catch (Exception e) {
			errorCallback.onError("Unexpected Exception: " + e.getMessage(), e);
		}
		return false;
	}

	private void initWebSocket(SockJSSocket sockJSSocket) {
		sockJSSocket.handler(c -> {
			// TODO - exception handling
			String message = c.toString();
			// Echo back to web client
			CompletableFuture.runAsync(() -> sendMessageToWS(helpeeName, message), executor);
			String smsg = "<b>" + helpeeName + " </b>" + message;
			CompletableFuture.runAsync(() -> sendMessageToSymphony(smsg), executor);
		});

		sockJSSocket.endHandler(c -> {
			sendMessageToSymphony(helpeeName + " has left the session.");
		});

		sockJSSocket.exceptionHandler(c -> {
			symClient.sendMessage(roomId,
					"There was a problem with the connection to [" + helpeeName + "] " + c.getMessage());
		});
	}

	public void sendMessageToWS(String from, String message) {
		Message m = new Message(System.currentTimeMillis(), from, message);
		transcription.add(m);
		String encodePrettily = Json.encodePrettily(m);
		logger.debug("Sending to WS Client: " + encodePrettily);
		Buffer b = Buffer.buffer(encodePrettily);
		sockJSSocket.write(b);
		logger.debug("Send complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.symphonyoss.HelpSession#addHelpAgent(java.lang.String, long)
	 */
	@Override
	public void addHelpAgent(String alias, String agentEmailAddress) {
		boolean owner = true;
		try {
			logger.info("adding agent to room: " + agentEmailAddress);
			Long symUserId = symClient.lookupUserByEmail(agentEmailAddress);
			if (symUserId == null) {
				throw new RuntimeException("Unable to find Agent by email address");
			}
			aliasMap.put(symUserId, alias);
			symClient.addMemberToRoom(roomId, symUserId, owner);
		} catch (org.symphonyoss.symphony.pod.invoker.ApiException e) {
			throw new RuntimeException(e);
		}
		sendMessageToWS(HelpBotConfig.HB_USER_NAME, alias + " has joined the room.");
		sendMessageToSymphony(helpRequest.toString());
	}

	private void sendMessageToSymphony(String message) {
		// Line feeds cause a problem, need to remove them.
		String cleanMsg = message.toString().replaceAll("\\n", " ").replaceAll("\\r", " ");
		String formattedMessage = "<messageML>" + cleanMsg + "</messageML>";
		symClient.sendMessage(roomId, formattedMessage);
	}

	@Override
	public void terminateSession() {
		sendMessageToWS(HelpBotConfig.HB_USER_NAME, HelpBotConfig.SESSION_TERMINATED_MESSAGE);
		// TODO - clean up the room?
		sockJSSocket.close();
		sendMessageToSymphony(HelpBotConfig.SESSION_TERMINATED_MESSAGE);
	}

	@Override
	public MultiMap getHelpRequest() {
		return helpRequest;
	}

	@Override
	public List<Message> getTranscription() {
		return Collections.unmodifiableList(transcription);
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

}
