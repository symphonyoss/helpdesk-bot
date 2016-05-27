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
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class NicknameChangedHandlerTest
{
	private NicknameChangedHandler nicknameChangedHandler;
	
	private Presence presence;
	
	private @Mocked @NonStrict Desk deskRoom;
	
	@Before
	public void before()
	{
		presence = new Presence();
		
		presence.setFrom(JIDUtils.getUserJID("user1"));
		presence.setTo(JIDUtils.getNicknameJID("desk1", "abc"));
		
		nicknameChangedHandler = new NicknameChangedHandler(presence, deskRoom);
	}
	
	@Test
	public void handle_SomeoneHasAlreadyUsedNickname_ReturnConflictError()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = true;
				
				PresenceResponse.createPresenceError(presence, PacketError.Condition.conflict); times = 1;
			}
		};
		
		nicknameChangedHandler.handle();
	}
	
	@Test
	public void handle_NicknameIsAvailableButSenderIsNotInDesk_ReturnItemNotFoundError()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.getOccupantByNickname(anyString); result = null;
				
				deskRoom.getOccupantByJID((JID)any); result = null;
				
				PresenceResponse.createPresenceError(presence, PacketError.Condition.item_not_found); times = 1;
			}
		};
		
		nicknameChangedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_NicknameIsAvailableAndSenderIsInDesk_ResponseLeaveForOldNicknameAndJoinForNewNickname()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			@Mocked @NonStrict
			User senderUser;
			{
				deskRoom.getOccupantByNickname(anyString); result = null;
				
				deskRoom.getOccupantByJID((JID)any); result = senderUser;
				
				PresenceResponse.createLeaveResponse(senderUser, (Collection<User>)any, (JID)any); times = 1;
				PresenceResponse.createLeaveResponse(senderUser, senderUser, (JID)any); times = 1;
				
				senderUser.setNickname("abc"); times = 1;
				
				PresenceResponse.createJoinResponse(senderUser, (Collection<User>)any, (JID)any, false); times = 1;
				PresenceResponse.createJoinResponse(senderUser, senderUser, (JID)any, false); times = 1;
			}
		};
		
		nicknameChangedHandler.handle();
	}
}
