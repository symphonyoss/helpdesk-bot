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

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class OccupantLeaveConversationHandlerTest
{
	private OccupantLeaveConversationHandler occupantLeaveConversationHandler;
	private Presence presence;
	private User admin;
	private User participant;
	
	private @Mocked @NonStrict
	Conversation conversation;
	
	
	@Before
	public void setUp() throws Exception
	{
		admin = UserCreator.createOwnerUser("admin01");
		participant = UserCreator.createOwnerUser("participant01");
		
		presence = new Presence();
		presence.setFrom(admin.getJID());
		presence.setTo(participant.getJID());
		presence.setID("1001");
		
		conversation.addOccupant(admin);
		conversation.addOccupant(participant);
		
		occupantLeaveConversationHandler = new OccupantLeaveConversationHandler(presence, conversation);
	}

	@Test
	public void handle_OccupantLeave()
	{
		new Expectations()
		{
			{
				conversation.getJID(); result = JIDUtils.getUserJID("Receiver1");
				conversation.getOccupantByJID((JID)any); result = participant;
			}
		};
		
		occupantLeaveConversationHandler.handle();
	}
	
	@Test
	public void handle_UnKnownOccupantLeave()
	{
		new Expectations()
		{
			{
				conversation.getJID(); result = JIDUtils.getUserJID("Receiver1");
				conversation.getOccupantByJID((JID)any); result = null;
			}
		};
		
		occupantLeaveConversationHandler.handle();
	}

}
