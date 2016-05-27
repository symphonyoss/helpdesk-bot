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
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NicknameChangedHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(NicknameChangedHandler.class);
	
	private Presence presence;

	private Desk deskRoom;

	public NicknameChangedHandler(Presence presence, Desk deskRoom)
	{
		this.presence = presence;
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", presence.getTo().toBareJID());
		
		String newNickname = presence.getTo().getResource();

		// Check the new nickname has already existed or not
		if (deskRoom.isNicknameExisted(newNickname))
		{
			packetList.add(PresenceResponse.createPresenceError(presence, PacketError.Condition.conflict));
			
			Logger.info("{} cannot change the nickname to {} because it has already been used by others in desk [{}].",
					new Object[] {presence.getFrom(), newNickname, deskRoom.getJID()});
			
			return;
		}

		JID senderJID = presence.getFrom();
		
		User senderUser = deskRoom.getOccupantByJID(senderJID);
		
		if (senderUser != null)
		{
			String oldNickname = senderUser.getNickname();
			
			// Remove user from the room
			deskRoom.removeOccupant(senderUser);

			// Send the old nickname leave the room to all admins
			packetList.addAll(PresenceResponse.createLeaveResponse(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID()));

			// Send the old nickname leave to sender
			packetList.add(PresenceResponse.createLeaveResponse(senderUser, senderUser, deskRoom.getJID()));

			senderUser.setNickname(newNickname);

			// Send update new nickname to all admins
			packetList.addAll(PresenceResponse.createJoinResponse(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID(), false));

			// Send update new nickname to sender
			packetList.add(PresenceResponse.createJoinResponse(senderUser, senderUser, deskRoom.getJID(), false));

			// Add user back to the room
			deskRoom.addOccupant(senderUser);
			
			Logger.info("{} has changed the nickname from {} to {} in desk [{}]",
					new Object[] {presence.getFrom(), oldNickname, newNickname, deskRoom.getJID()});
		}
		else
		{
			packetList.add(PresenceResponse.createPresenceError(presence, PacketError.Condition.item_not_found));
			
			Logger.info("{} cannot change the nickname because user is not currently in desk [{}]",
					presence.getFrom(), deskRoom.getJID());
		}
	}
}
