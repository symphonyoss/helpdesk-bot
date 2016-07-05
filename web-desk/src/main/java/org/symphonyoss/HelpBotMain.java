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

package org.symphonyoss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import org.symphonyoss.cim.CIMImpl;
import org.symphonyoss.sapi.ServiceAPI;
import org.symphonyoss.session.HelpSession;
import org.symphonyoss.util.SSLUtil;
import org.symphonyoss.web.HelpBotWebServer;
import org.symphonyoss.web.HelpSessionListener;

import io.vertx.core.MultiMap;

public class HelpBotMain {

	public static final Logger log = LoggerFactory.getLogger(HelpBotMain.class);

	public static void main(String[] args) throws Exception {

		CommandLine command = new CommandLine();
		JCommander jc = new JCommander(command);
		try {
			jc.parse(args);
		} catch (Exception e) {
			jc.usage();
			return;
		}

		log.info("Starting HelpBot Server...");

		HelpBotConfig config = HelpBotConfig.init(command.configFile);
		// disableSSLCertCheck();
		if (command.sslTrustAllHosts) {
			log.warn("********* Removing SSL Server Certification Validation **********");
			SSLUtil.setupSSL();
		}

		new HelpBotMain(config);
	}

	public HelpBotMain(HelpBotConfig config) {
		// Symphony Service API Wrapper.
		ServiceAPI serviceAPI = new ServiceAPI(config.getPodUrl(), config.getAgentUrl(), config);

		// This is the integration point with CIM.
		AgentSelector agentSelector = new CIMImpl();

		HelpSessionListener hsl = new HelpSessionListener() {

			@Override
			public void onHelpSessionInit(HelpSession session) {
				// The helpRequest contains whatever parameters are POSTed to
				// http://host/help
				// This can customized in the help front page.
				MultiMap helpRequest = session.getHelpRequest();
				log.info("Help Session Initiated. Requesting Agent Assignment. " + helpRequest);

				// The Agent Selector is where BLK will notify CIM of the help
				// request, and wait for an agent to accept the request.
				// At that point, the agent is added to the session, which in
				// turn adds the agent to the Symphony Room.
				// This room will contain the agent and the HelpBot'
				// Messages sent from the helpee will appear in the room, and
				// messages sent into the room by the agent will
				// be forwarded to the helpee chat component.
				agentSelector.assignAgent(helpRequest,
						(agent) -> session.addHelpAgent(agent.getName(), agent.getEmail()));

			}

			@Override
			public void onHelpSessionTerminate(HelpSession helpSession) {
				// this is fired when the Helpee session has been terminated.
				// you might wanto to mail the transcription somewhere.
				helpSession.getTranscription();
			}
		};

		// Start the web server. The webserver will host the chat assets for the
		// helpee UI, as
		// well as an WS endpoint supporting the chat interaction with helpbot.
		new HelpBotWebServer(config, serviceAPI, hsl);
		log.info("Started HelpBot Server on port: " + config.getPort());
	}

}
