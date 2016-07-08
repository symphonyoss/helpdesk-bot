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

package org.symphonyoss.webroomdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.webroomdesk.constants.HelpBotConstants;
import org.symphonyoss.webroomdesk.utils.CallCache;

/**
 * Created by nicktarsillo on 6/15/16.
 * A thread in charge of adding inactivity time.
 */
public class InactivityThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(InactivityThread.class);
    private boolean stop;

    public InactivityThread() {

    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(HelpBotConstants.INACTIVITY_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("Inactivity tick triggered. Adding {} miliseconds.", HelpBotConstants.INACTIVITY_INTERVAL);
            CallCache.checkCallInactivity(HelpBotConstants.INACTIVITY_INTERVAL);
        }
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
