package com.symphony;

import com.symphony.sapi.AsyncCallback;

import io.vertx.core.MultiMap;

public interface AgentSelector {

	void assignAgent(MultiMap helpRequest, AsyncCallback<AgentInfo> callback);

}
