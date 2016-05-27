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
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class GetOccupantListHandlerTest
{
	private GetOccupantListHandler getOccupantListHandler;
	
	private IQ iqRequest;
	
	private @Mocked @NonStrict Desk deskRoom;

	@Before
	public void before()
	{
		iqRequest = new IQ();
		iqRequest.setFrom(JIDUtils.getUserJID("Sender1"));
		iqRequest.setTo(JIDUtils.getUserJID("Receiver1"));

		getOccupantListHandler = new GetOccupantListHandler(iqRequest, deskRoom);
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsParticipantAndIsNotInDesk_ReturnOnlyVirtualDeskUserInList()
	{
		new Expectations()
		{
			@Mocked List<User> userList;
			@Mocked @NonStrict
			IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = false;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				deskRoom.getOccupantByJID((JID)any); result = null;
				
				userList = new ArrayList<User>();
				
				userList.add(virtualDeskUser);
				
				IQResponse.createGetOccupantList((IQ)any, (Collection<User>)any);
			}
		};
		
		getOccupantListHandler.handle();
	}

	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsParticipantAndIsInDesk_ReturnVirtualDeskUserAndThatUserInList()
	{
		new Expectations()
		{
			@Mocked List<User> userList;
			@Mocked @NonStrict IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = false;
				
				User virtualDeskUser = new User(JIDUtils.getUserJID("desk1"), "desk1");
				User senderUser = new User(iqRequest.getFrom(), "sender1");
				
				deskRoom.getVirtualDeskUser(); result = virtualDeskUser;
				
				deskRoom.getOccupantByJID((JID)any); result = senderUser;
				
				userList = new ArrayList<User>();
				
				userList.add(virtualDeskUser);
				userList.add(senderUser);
				
				IQResponse.createGetOccupantList((IQ)any, (Collection<User>)any);
			}
		};
		
		getOccupantListHandler.handle();
	}

	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsAdminAndIsInDesk_ReturnBothDeskMembersAndParticipants()
	{
		new Expectations()
		{
			@Mocked List<User> userList;
			@Mocked @NonStrict IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = true;
				
				List<User> memberList = new ArrayList<User>();
				List<User> participantList = new ArrayList<User>();
				
				deskRoom.getCurrentMembers(); result = memberList;
				deskRoom.getCurrentParticipants(); result = participantList;
				
				userList = new ArrayList<User>();
				
				userList.addAll(memberList);
				userList.addAll(participantList);
				
				IQResponse.createGetOccupantList((IQ)any, (Collection<User>)any);
			}
		};
		
		getOccupantListHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_UserIsOwnerAndIsInDesk_ReturnBothDeskMembersAndParticipants()
	{
		new Expectations()
		{
			@Mocked List<User> userList;
			@Mocked @NonStrict IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = true;
				
				List<User> memberList = new ArrayList<User>();
				List<User> participantList = new ArrayList<User>();
				
				deskRoom.getCurrentMembers(); result = memberList;
				deskRoom.getCurrentParticipants(); result = participantList;
				
				userList = new ArrayList<User>();
				
				userList.addAll(memberList);
				userList.addAll(participantList);
				
				IQResponse.createGetOccupantList((IQ)any, (Collection<User>)any);
			}
		};
		
		getOccupantListHandler.handle();
	}
}
