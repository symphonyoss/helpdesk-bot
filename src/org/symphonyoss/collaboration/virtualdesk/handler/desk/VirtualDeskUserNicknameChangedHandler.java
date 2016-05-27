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

import java.util.Collection;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualDeskUserNicknameChangedHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(VirtualDeskUserNicknameChangedHandler.class);

	private Desk deskRoom;
	
	private User oldVirtualDeskUser;
	private User newVirtualDeskUser;

	public VirtualDeskUserNicknameChangedHandler(User oldVirtualDeskUser, User newVirtualDeskUser, Desk deskRoom)
	{
		this.oldVirtualDeskUser = oldVirtualDeskUser;
		this.newVirtualDeskUser = newVirtualDeskUser;
		this.deskRoom = deskRoom;
	}
	
	@Override
	protected void internalHandle()
	{
		if (!oldVirtualDeskUser.getNickname().equalsIgnoreCase(newVirtualDeskUser.getNickname()))
		{
			Collection<User> participantCollection = deskRoom.getCurrentParticipants();
			
			if (participantCollection.size() > 0)
			{
				Logger.info("Update Virtual Desk user's nickname changed to all participant in desk");
				
				packetList.addAll(PresenceResponse.createNicknameChange(newVirtualDeskUser, oldVirtualDeskUser.getNickname(), participantCollection, deskRoom.getJID()));
				
				packetList.addAll(PresenceResponse.createJoinResponse(newVirtualDeskUser, participantCollection, deskRoom.getJID(), false));
			}
		}
	}
}
