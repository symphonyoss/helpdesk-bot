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
import java.util.HashSet;
import java.util.Set;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;

import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;

public class OccupantJoinedHandlerTest
{
	private OccupantJoinedHandler occupantJoinedHandler;
	
	private Presence presence;
	
	private @Mocked @NonStrict Desk deskRoom;

	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	

	
	@Before
	public void before()
	{
		presence = new Presence();
		
		presence.setFrom(JIDUtils.getUserJID("user1"));
		presence.setTo(JIDUtils.getNicknameJID("desk1", "abc"));

		occupantJoinedHandler = new OccupantJoinedHandler(presence, deskRoom, serviceConfiguration);
	}

	@Test
	public void handle_UserIsParticipantAndDeskIsMembersOnlyButUserIsNotMember_ResponsePresenceError()
	{
		new Expectations()
		{
			@Mocked PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;

				deskRoom.isOwner(anyString); result = false;
				deskRoom.isAdmin(anyString); result = false;
				deskRoom.isMember(anyString); result = false;
				
				deskRoom.isMembersOnly(); result = true;
				
				deskRoom.getParticipants(); result = new HashSet<String>();
				
				PresenceResponse.createPresenceError((User)any, (JID)any, PacketError.Condition.not_allowed); times = 1;
			}
		};
		
		occupantJoinedHandler.handle();
	}

	@Test
	public void handle_UserIsParticipantAndDeskIsMembersAndUserIsMember_ReturnJoinResponseAndPresenceUpdate()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;

				deskRoom.isOwner(anyString); result = false;
				deskRoom.isAdmin(anyString); result = false;
				deskRoom.isMember(anyString); result = false;
				
				deskRoom.isMembersOnly(); result = true;
				
				Set<String> participantSet = new HashSet<String>();
				participantSet.add(presence.getFrom().toBareJID());
				
				deskRoom.getParticipants(); result = participantSet;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				PresenceResponse.createJoinResponse(virtualDeskUser, (User)any, (JID)any, false); times = 1;
				PresenceResponse.createPresenceUpdate(virtualDeskUser, (User)any, (JID)any, (PresenceType)any); times = 1;
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = new Presence();
			}
		};
		
		occupantJoinedHandler.handle();
	}

	@Test
	public void handle_UserIsParticipantAndDeskIsNotMembers_ReturnJoinResponseAndPresenceUpdate()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;

				deskRoom.isOwner(anyString); result = false;
				deskRoom.isAdmin(anyString); result = false;
				deskRoom.isMember(anyString); result = false;
				
				deskRoom.isMembersOnly(); result = false;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				PresenceResponse.createJoinResponse(virtualDeskUser, (User)any, (JID)any, false); times = 1;
				PresenceResponse.createPresenceUpdate(virtualDeskUser, (User)any, (JID)any, (PresenceType)any); times = 1;
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = new Presence();
			}
		};
		
		occupantJoinedHandler.handle();
	}

	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsAdminAndIsFirstDeskMember_ReturnJoinResponseAndVirtualDeskUserPresenceUpdate()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;

				deskRoom.isOwner(anyString); result = false;
				deskRoom.isAdmin(anyString); result = true;
				deskRoom.isMember(anyString); result = false;
				
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 2;
				
				deskRoom.getCurrentMemberCount(); result = 0;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				PresenceResponse.createPresenceUpdate(virtualDeskUser, (Collection<User>)any, (JID)any, PresenceType.Online); times = 1;
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = new Presence();
			}
		};
		
		occupantJoinedHandler.handle();
	}

	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsAdminAndIsNotFirstDeskMember_ReturnJoinResponse()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;

				deskRoom.isOwner(anyString); result = false;
				deskRoom.isAdmin(anyString); result = true;
				deskRoom.isMember(anyString); result = false;
				
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 2;
				
				deskRoom.getCurrentMemberCount(); result = 1;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				PresenceResponse.createPresenceUpdate(virtualDeskUser, (Collection<User>)any, (JID)any, PresenceType.Online); times = 0;
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = new Presence();
			}
		};
		
		occupantJoinedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsOwnerAndIsNotFirstDeskMember_ReturnJoinResponse()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;

				deskRoom.isOwner(anyString); result = true;
				deskRoom.isAdmin(anyString); result = false;
				deskRoom.isMember(anyString); result = false;
				
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 2;
				
				deskRoom.getCurrentMemberCount(); result = 1;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				PresenceResponse.createPresenceUpdate(virtualDeskUser, (Collection<User>)any, (JID)any, PresenceType.Online); times = 0;
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = new Presence();
			}
		};
		
		occupantJoinedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsMemberAndIsNotFirstDeskMember_ReturnJoinResponse()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = false;
				
				deskRoom.isOwner(anyString); result = false;
				deskRoom.isAdmin(anyString); result = false;
				deskRoom.isMember(anyString); result = true;
				
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 2;
				
				deskRoom.getCurrentMemberCount(); result = 1;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				PresenceResponse.createPresenceUpdate(virtualDeskUser, (Collection<User>)any, (JID)any, PresenceType.Online); times = 0;
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = new Presence();
			}
		};
		
		occupantJoinedHandler.handle();
	}
	
	@Test
	public void handle_UserJoinsWithExistingNicknameInDesk_ResponseJoinErrorWithConflict()
	{
		final User user = new User(new JID("owner@virtualdesktest.com"), "owner");
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				deskRoom.isNicknameExisted(anyString); result = true;
				deskRoom.getOccupantByNickname(anyString); result= user;
				PresenceResponse.createPresenceError((Presence)any, PacketError.Condition.conflict);
			}
		};
		
		occupantJoinedHandler.handle();
	}
}
