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
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OccupantLeaveHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(OccupantLeaveHandler.class);
	
	private Presence presence;

	private Desk deskRoom;

	public OccupantLeaveHandler(Presence presence, Desk deskRoom)
	{
		this.presence = presence;
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		JID senderJID = presence.getFrom();

		User senderUser = deskRoom.getOccupantByJID(senderJID);
		
		MDC.put("deskjid", presence.getTo().toBareJID());

		if (senderUser != null)
		{
			deskRoom.removeOccupant(senderUser);

			// Send leave response to him/herself
			packetList.add(PresenceResponse.createLeaveResponse(senderUser, senderUser, deskRoom.getJID()));

			// Notify all members that there is user left the room
			packetList.addAll(PresenceResponse.createLeaveResponse(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID()));
		
			Logger.info("{} has left the desk [{}]", senderJID, deskRoom.getJID());
			

		}

		if (deskRoom.getCurrentMemberCount() == 0)
		{
			Logger.debug("Updating virtual desk user's presence to all participants in desk [{}] because there is no desk member left",
					deskRoom.getJID());

			// If there is no member left in the desk, Virtual Desk will notify the desk presence changed 
			// to participants in the desk
			packetList.addAll(PresenceResponse.createPresenceUpdate(deskRoom.getVirtualDeskUser(),
					deskRoom.getCurrentParticipants(),
					deskRoom.getJID(),
					deskRoom.getDeskPresence()));
		}
	}
}
