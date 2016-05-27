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

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class GetDeskInfoHandlerTest
{
	private GetDeskInfoHandler getDeskInfoHandler;
	
	private IQ iqRequest;
	
	private @Mocked @NonStrict Desk deskRoom;

	@Before
	public void before()
	{
		iqRequest = new IQ();
		iqRequest.setFrom(JIDUtils.getUserJID("Sender1"));
		iqRequest.setTo(JIDUtils.getUserJID("Receiver1"));

		getDeskInfoHandler = new GetDeskInfoHandler(iqRequest, deskRoom);
	}
	
	@Test
	public void handle_UserIsParticipantButIsNotInDesk_CallCreateDeskInfoWithOneOccupant()
	{
		new Expectations()
		{
			@Mocked @NonStrict IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = false;
				
				deskRoom.getOccupantByJID((JID)any); result = null;
				
				IQResponse.createDeskInfo((IQ)any, anyString, anyString, 1); times = 1;
			}
		};

		getDeskInfoHandler.handle();
	}
	
	@Test
	public void handle_UserIsParticipantAndIsInDesk_CallCreateDeskInfoWithTwoOccupants()
	{
		new Expectations()
		{
			@Mocked @NonStrict IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = false;
				
				deskRoom.getOccupantByJID((JID)any); result = new User(JIDUtils.getUserJID("user1"), "user1");
				
				IQResponse.createDeskInfo((IQ)any, anyString, anyString, 2); times = 1;
			}
		};

		getDeskInfoHandler.handle();
	}
	
	@Test
	public void handle_UserHasMemberPrivilege_CallCreateDeskInfoWithOneOccupant()
	{
		new Expectations()
		{
			@Mocked @NonStrict IQResponse iqResponse;
			{
				deskRoom.hasDeskMemberPrivilege(anyString); result = true;
				
				int memberCount = 2;
				int participantCount = 7;
				
				deskRoom.getCurrentMemberCount(); result = memberCount;
				deskRoom.getCurrentParticipantCount(); result = participantCount;
				
				IQResponse.createDeskInfo((IQ)any, anyString, anyString, memberCount + participantCount); times = 1;
			}
		};

		getDeskInfoHandler.handle();
	}
}
