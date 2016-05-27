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
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.JID;

public class InviteRejectConversationHandlerTest
{
	private @Mocked @NonStrict
	Conversation conversation;
	private JID senderJID;
	private InviteRejectConversationHandler inviteRejectConversationHandler;

	@Before
	public void setUp() throws Exception
	{
		senderJID = new JID("test","domain","resource");
		
		inviteRejectConversationHandler = new InviteRejectConversationHandler(conversation, senderJID);
	}

	@Test
	public void handle_MemberRejectInvite()
	{
		new Expectations()
		{
			{
				conversation.getJID(); result = JIDUtils.getUserJID("Receiver1");
				conversation.hasDeskMemberPrivilege(anyString); result = true;
			}
		};
		
		inviteRejectConversationHandler.handle();
	}
	
	@Test
	public void handle_ParticipantRejectInvite()
	{
		new Expectations()
		{
			{
				conversation.getJID(); result = JIDUtils.getUserJID("Receiver1");
				conversation.hasDeskMemberPrivilege(anyString); result = false;
				conversation.getMemberJoined(); result = true;
			}
		};
		
		inviteRejectConversationHandler.handle();
	}

}
