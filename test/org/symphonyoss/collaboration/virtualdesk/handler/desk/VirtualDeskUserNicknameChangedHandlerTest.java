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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class VirtualDeskUserNicknameChangedHandlerTest
{
	private VirtualDeskUserNicknameChangedHandler deskNicknameChangedHandler;
	
	private @Mocked @NonStrict
	User oldVirtualDeskUser;
	private @Mocked @NonStrict User newVirtualDeskUser;
	private @Mocked @NonStrict Desk deskRoom;
	
	@Before
	public void before()
	{
		deskNicknameChangedHandler = new VirtualDeskUserNicknameChangedHandler(oldVirtualDeskUser, newVirtualDeskUser, deskRoom);
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_DeskAliasNameIsNotChanged_DoNothing()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				oldVirtualDeskUser.getNickname(); result = "user1";
				newVirtualDeskUser.getNickname(); result = "user1";
				
				PresenceResponse.createNicknameChange((User)any, anyString, (Collection<User>)any, (JID)any); times = 0;
			}
		};
		
		deskNicknameChangedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_DeskAliasNameIsChangedSomeCharactersToCapitalLetters_DoNothing()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				oldVirtualDeskUser.getNickname(); result = "user1";
				newVirtualDeskUser.getNickname(); result = "UsEr1";
				
				PresenceResponse.createNicknameChange((User)any, anyString, (Collection<User>)any, (JID)any); times = 0;
			}
		};
		
		deskNicknameChangedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_DeskAliasNameIsChangedButNoParticipantInDesk_DoNothing()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				oldVirtualDeskUser.getNickname(); result = "user1";
				newVirtualDeskUser.getNickname(); result = "user1_new";
				
				deskRoom.getCurrentParticipants(); result = new ArrayList<User>();
				
				PresenceResponse.createNicknameChange((User)any, anyString, (Collection<User>)any, (JID)any); times = 0;
			}
		};
		
		deskNicknameChangedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_DeskAliasNameIsChangedAndHaveParticipantInDesk_DoNothing()
	{
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				oldVirtualDeskUser.getNickname(); result = "user1";
				newVirtualDeskUser.getNickname(); result = "user1_new";
				
				List<User> participantList = new ArrayList<User>();
				participantList.add(new User(JIDUtils.getUserJID("part1"), "part1"));
				
				deskRoom.getCurrentParticipants(); result = participantList;
				
				PresenceResponse.createNicknameChange((User)any, anyString, (Collection<User>)any, (JID)any); times = 1;
				PresenceResponse.createJoinResponse((User)any, (Collection<User>)any, (JID)any, false); times = 1;
			}
		};
		
		deskNicknameChangedHandler.handle();
	}
}
