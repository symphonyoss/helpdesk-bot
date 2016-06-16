package com.symphony.sapi;

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
import com.symphony.HelpBotConfig;
import com.symphony.api.api.MessagesApi;
import com.symphony.api.api.RoomMembershipApi;
import com.symphony.api.api.SessionApi;
import com.symphony.api.api.StreamsApi;
import com.symphony.api.api.UsersApi;
import com.symphony.api.client.ApiClient;
import com.symphony.api.client.ApiException;
import com.symphony.api.model.ImmutableRoomAttributes;
import com.symphony.api.model.Message;
import com.symphony.api.model.MessageList;
import com.symphony.api.model.MessageSubmission;
import com.symphony.api.model.MessageSubmission.FormatEnum;
import com.symphony.api.model.RoomAttributes;
import com.symphony.api.model.RoomCreate;
import com.symphony.api.model.RoomDetail;
import com.symphony.api.model.SessionInfo;
import com.symphony.api.model.User;
import com.symphony.api.model.UserId;

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
		ApiClient agentClient = new ApiClient();
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
	 * @param roomName
	 *            The Room name to be created.
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
			msg.setFormat(FormatEnum.MESSAGEML);
			messagesApi.v1StreamSidMessageCreatePost(streamId, sessionToken, kmSession, msg);
			logger.info("sent Message to Symphony:  " + streamId + " " + message);

		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Long lookupUserByEmail(String agentEmailAddress) throws ApiException {
		logger.info("Looking up user:   [" + agentEmailAddress + "]");

		User user = this.userLookupApi.v1UserGet(agentEmailAddress, sessionToken);
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
					} catch (ApiException e) {
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
