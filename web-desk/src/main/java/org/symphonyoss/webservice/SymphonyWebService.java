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

package org.symphonyoss.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.webdesk.config.WebBotConfig;
import org.symphonyoss.webservice.listeners.SessionListener;
import org.symphonyoss.webservice.models.session.Session;
import org.symphonyoss.webservice.models.web.WebServer;
import org.symphonyoss.webservice.util.SSLUtil;

import java.util.HashSet;
import java.util.Set;

public class SymphonyWebService {

    public static final Logger logger = LoggerFactory.getLogger(SymphonyWebService.class);

    private Set<SessionListener> externalSessionListeners = new HashSet<>();

    public SymphonyWebService() {
        logger.info("Starting web service");

        // disableSSLCertCheck();
        if (Boolean.parseBoolean(System.getProperty(WebBotConfig.TRUST_ALL_SSL))) {
            logger.warn("********* Removing SSL Server Certification Validation **********");
            try {
                SSLUtil.setupSSL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // This is the integration point with CIM.

        SessionListener mainSessionListener = new SessionListener() {

            @Override
            public void onSessionInit(Session session) {

                for (SessionListener sessionListener : externalSessionListeners)
                    sessionListener.onSessionInit(session);

            }

            @Override
            public void onSessionTerminate(Session helpSession) {

                // this is fired when the session has been terminated.
                helpSession.getTranscription();

            }
        };

        // Start the web server.
        new WebServer(mainSessionListener);
    }

    public void registerListener(SessionListener sessionListener) {
        externalSessionListeners.add(sessionListener);
    }

    public void removeListener(SessionListener sessionListener) {
        externalSessionListeners.remove(sessionListener);
    }
}
