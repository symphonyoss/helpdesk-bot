package com.symphony.session;

import java.util.List;

import com.symphony.web.Message;

import io.vertx.core.MultiMap;

public interface HelpSession {

	void addHelpAgent(String alias, String agentEmailAddress);

	void terminateSession();

	MultiMap getHelpRequest();

	List<Message> getTranscription();

	boolean isTerminated();

}