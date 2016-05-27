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
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;

public class PresenceChangedHandler extends AbstractActionHandler
{
	private Presence presence;

	private Desk deskRoom;

	public PresenceChangedHandler(Presence presence, Desk deskRoom)
	{
		this.presence = presence;
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", deskRoom.getJID());
		
		JID senderJID = presence.getFrom();

		User senderUser = deskRoom.getOccupantByJID(senderJID);
		
		senderUser.setPresence(PresenceType.getPresence(presence.getStatus()));

		if (senderUser.isDeskMember())
		{
			//Send presence to admin as current user presence
			packetList.addAll(PresenceResponse.createPresenceUpdate(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID(), presence));
			
			//Send presence to room participant as room presence
			packetList.addAll(PresenceResponse.createPresenceUpdate(deskRoom.getVirtualDeskUser(), deskRoom.getCurrentParticipants(), deskRoom.getJID(), deskRoom.getDeskPresence()));
		}
		else
		{
			//Occupant join room send Presence to room admin 
			packetList.addAll(PresenceResponse.createPresenceUpdate(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID(), presence));
			
			//send presence to itselft
			packetList.add(PresenceResponse.createPresenceUpdate(senderUser, senderUser, deskRoom.getJID(), senderUser.getPresence()));
		}
	}
}
