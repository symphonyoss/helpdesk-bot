package com.symphony.session;

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

import com.symphony.HelpBotConfig;
import com.symphony.api.client.ApiException;
import com.symphony.sapi.AsyncCallback;
import com.symphony.sapi.ErrorCallback;
import com.symphony.sapi.ServiceAPI;
import com.symphony.sapi.StreamListener;
import com.symphony.sapi.SymphonyMessage;
import com.symphony.web.Message;

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

	private ServiceAPI sapi;

	private Map<Long, String> aliasMap = new HashMap<>();
	private String roomId;
	private MultiMap helpRequest;
	private String helpeeName;
	private List<Message> transcription = new ArrayList<>();

	private SockJSSocket sockJSSocket;
	private Executor executor = Executors.newFixedThreadPool(20);

	private String terminationString;

	public static HelpSession init(SockJSSocket sockJSSocket, MultiMap helpRequest, ServiceAPI sapi, String username,
			AsyncCallback<HelpSession> readyCallback, ErrorCallback errorCallback) {
		return new HelpSessionImpl(sockJSSocket, helpRequest, sapi, username, readyCallback, errorCallback);
	}

	private HelpSessionImpl(SockJSSocket sockJSSocket, MultiMap helpRequest, ServiceAPI sapi, String username,
			AsyncCallback<HelpSession> readyCallback, ErrorCallback errorCallback) {
		this.helpRequest = helpRequest;
		this.helpeeName = username;
		this.sapi = sapi;
		this.sockJSSocket = sockJSSocket;
		this.terminationString = "/close";
		String roomName = HelpBotConfig.HB_ROOM_PREFIX + UUID.randomUUID().toString();

		initWebSocket(sockJSSocket);

		sendMessageToWS(HelpBotConfig.HB_USER_NAME, HelpBotConfig.PLEASE_WAIT);

		StreamListener symphonyMessageListener = (m) -> handleMessageFromSymphony(m);
		// Do this asynchronously
		CompletableFuture.runAsync(
				() -> createRoom(sapi, roomName, symphonyMessageListener, readyCallback, errorCallback), executor);
	}

	private void handleMessageFromSymphony(SymphonyMessage m) {
		if (m.getFrom() == sapi.getSymphonyUserId()) {
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

	private boolean createRoom(ServiceAPI sapi, String roomName, final StreamListener symphonyMessageListener,
			AsyncCallback<HelpSession> readyCallback, ErrorCallback errorCallback) {
		try {
			// Save the Symphony Room Id
			roomId = sapi.createRoom(roomName);
			// "subscribe" to messages in this room
			sapi.addStreamListener(roomId, symphonyMessageListener);
			// The session is ready now, tell the caller of "init"
			readyCallback.callback(HelpSessionImpl.this);
			// Send a welcome message to the WS
			sendMessageToWS(HelpBotConfig.HB_USER_NAME, HelpBotConfig.WELCOME_MSG);
			return true;
		} catch (ApiException e) {
			errorCallback.onError("Unable to create room: " + e.getMessage(), e);
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
			sapi.sendMessage(roomId,
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
	 * @see com.symphony.HelpSession#addHelpAgent(java.lang.String, long)
	 */
	@Override
	public void addHelpAgent(String alias, String agentEmailAddress) {
		boolean owner = true;
		try {
			logger.info("adding agent to room: " + agentEmailAddress);
			Long symUserId = sapi.lookupUserByEmail(agentEmailAddress);
			if (symUserId == null) {
				throw new RuntimeException("Unable to find Agent by email address");
			}
			aliasMap.put(symUserId, alias);
			sapi.addMemberToRoom(roomId, symUserId, owner);
		} catch (ApiException e) {
			throw new RuntimeException(e);
		}
		sendMessageToWS(HelpBotConfig.HB_USER_NAME, alias + " has joined the room.");
		sendMessageToSymphony(helpRequest.toString());
	}

	private void sendMessageToSymphony(String message) {
		// Line feeds cause a problem, need to remove them.
		String cleanMsg = message.toString().replaceAll("\\n", " ").replaceAll("\\r", " ");
		String formattedMessage = "<messageML>" + cleanMsg + "</messageML>";
		sapi.sendMessage(roomId, formattedMessage);
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
