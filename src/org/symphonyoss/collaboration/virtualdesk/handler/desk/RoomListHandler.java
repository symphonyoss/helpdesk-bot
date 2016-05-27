/*
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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.collaboration.virtualdesk.handler.desk;

import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.IQ;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomListHandler extends AbstractActionHandler
{
	private static Logger Logger = LoggerFactory.getLogger(RoomListHandler.class);
	
	private IDeskDirectory deskDirectory;

	private IQ iqRequest;

	public RoomListHandler(IQ iqRequest, IDeskDirectory deskDirectory)
	{
		this.iqRequest = iqRequest;
		this.deskDirectory = deskDirectory;
	}

	@Override
	protected void internalHandle()
	{
		packetList.add(IQResponse.createRoomListing(iqRequest, deskDirectory.getAllDesks()));
		
		Logger.info("{} has requested desk list.", iqRequest.getFrom().toString());
	}
}
