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

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.handler.IActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;


public class CreateDeskHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(CreateDeskHandler.class);
	
	private Presence presence;

	private IDeskDirectory deskDirectory;

	private IServiceConfiguration serviceConfiguration;

	public CreateDeskHandler(Presence presence, IDeskDirectory deskDirectory, IServiceConfiguration serviceConfiguration)
	{
		this.presence = presence;
		this.deskDirectory = deskDirectory;
		this.serviceConfiguration = serviceConfiguration;
	}

	@Override
	protected void internalHandle()
	{
		JID senderJID = presence.getFrom();
		
		JID deskJID = presence.getTo();

		int currentNumOfDesks = deskDirectory.getCurrentNumberOfDeskCreated(senderJID.toBareJID());

		MDC.put("deskjid", presence.getTo().toBareJID());

		if (currentNumOfDesks >= serviceConfiguration.getMaxNumberOfDeskCreated())
		{
			packetList.add(PresenceResponse.createPresenceError(presence, 
					PacketError.Condition.resource_constraint, 
					"You have reached the maximum number of virtual desks you can create."));

			Logger.info("{} cannot create the new desk because the number of desks that user created has reached the maximum limit.", senderJID);
			
			return;
		}

		Desk deskRoom = deskDirectory.createDesk(deskJID.getNode(), senderJID.toBareJID());

		// Add user who creates desk to owner list
		String senderBareJID = senderJID.toBareJID();
		deskRoom.addOwner(senderBareJID);

		Logger.info("{} has created the desk [{}] [NumOfDesks: {}]", new Object[] {senderJID, deskRoom.getJID(), currentNumOfDesks+1});
		

		IActionHandler occupantJoinedHandler = new OccupantJoinedHandler(presence, deskRoom, serviceConfiguration);
		packetList.addAll(occupantJoinedHandler.handle());

		deskRoom.init();
	}
}
