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

package org.symphonyoss.collaboration.virtualdesk.handler.conversation;

import java.util.Collection;
import java.util.Date;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;

public class OccupantJoinedConversationHandlerTest
{
	private OccupantJoinedConversationHandler occupantJoinedConversationHandler;
	private Presence presence;
	private User admin;
	private User participant;
	
	private @Mocked @NonStrict
	Conversation conversationRoom;

	@Before
	public void setUp() throws Exception
	{
		admin = UserCreator.createOwnerUser("admin01");
		participant = UserCreator.createOwnerUser("participant01");
		
		presence = new Presence();
		presence.setFrom(admin.getJID());
		presence.setTo(participant.getJID());
		presence.setID("1001");
		
		occupantJoinedConversationHandler = new OccupantJoinedConversationHandler(presence, conversationRoom);
	}

	@Test
	public void handle_NicknameNotExisted()
	{
		new Expectations()
		{
			{
				conversationRoom.isNicknameExisted(anyString); result = true;
			}
		};
		
		occupantJoinedConversationHandler.handle();
	}
	
	@Test
	public void handle_DoNotHasDeskMemberPrivilege()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 0;
				
				Presence blankPresence = new Presence();
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = blankPresence;
				
				conversationRoom.getQuestion(); result = new UserState();
				
				MessageResponse.createHistoryMessage("sender", admin.getJID().toBareJID(),anyString, new Date(), JIDUtils.getUserJID("user1")); times = 0;
				
				conversationRoom.getMemberRejectInvite(); result = true;
			}
		};
		
		occupantJoinedConversationHandler.handle();
	}
	
	@Test
	public void handle_HasDeskMemberPrivilege()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				conversationRoom.hasDeskMemberPrivilege(anyString); result = true;
				
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 2;
				
				Presence blankPresence = new Presence();
				UserState blankUserState = new UserState();
				
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = blankPresence;
				
				conversationRoom.getQuestion(); result = blankUserState;
				
				MessageResponse.createHistoryMessage("sender", admin.getJID().toBareJID(),anyString, new Date(), JIDUtils.getUserJID("user1")); times = 0; 
			}
		};
		
		occupantJoinedConversationHandler.handle();
	}
	
	@Test
	public void handle_DoNotHasDeskMemberPrivilegeAndMember()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				conversationRoom.hasDeskMemberPrivilege(anyString); result = false;
				conversationRoom.isMembersOnly(); result = true;
				
				Presence blankPresence = new Presence();
				
				PresenceResponse.createJoinResponse((Collection<User>)any, (User)any, (JID)any, false); times = 0;
				PresenceResponse.createJoinResponse((User)any, (User)any, (JID)any, anyBoolean); result = blankPresence;
				
				conversationRoom.getQuestion(); result = new UserState();
				
				MessageResponse.createHistoryMessage("sender", admin.getJID().toBareJID(),anyString, new Date(), JIDUtils.getUserJID("user1")); times = 0;
			}
		};
		
		occupantJoinedConversationHandler.handle();
	}

}
