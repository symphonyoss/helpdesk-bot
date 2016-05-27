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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.command.ICommand;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;

public class MemberMessageReceivedConversationHandlerTest
{
	private MemberMessageReceivedConversationHandler memberMessageReceivedConversationHandler;

	private Message message;
	private @Mocked @NonStrict
	Conversation conversationRoom;
	private @Mocked @NonStrict
	IDeskDirectory deskDirectory;
	private @Mocked @NonStrict
	ICommand command;
	
	@Before
	public void setUp() throws Exception
	{
		message = new Message();
		
		message.setFrom(JIDUtils.getUserJID("user1"));
		message.setTo(JIDUtils.getNicknameJID("desk1", null));
		message.setBody("test");
		
		memberMessageReceivedConversationHandler = new MemberMessageReceivedConversationHandler(message, conversationRoom, deskDirectory);
	}

	@Test
	public void handle_MessageBodyIsCommand_CallProcessCommand()
	{
		message.setBody("@command");
		
		
		new Expectations()
		{
			{
				conversationRoom.getOccupantByJID((JID) any); result = UserCreator.createOwnerUser("User01");
				conversationRoom.getJID(); result = JIDUtils.getUserJID("conversation1");
				
				command.process(anyString, (User)any, (Desk)any, (IDeskDirectory) any);
			}
		};
		
		memberMessageReceivedConversationHandler.handle();
	}
	
	@Test
	public void handle_MessageBodyNoCommand()
	{
		message.setBody("nocommand");
		
		
		new Expectations()
		{
			@Mocked MessageResponse messageResponse;
			{
				command.process(anyString, (User)any, (Desk)any, null); times = 0;
				
				List<User> memberList = new ArrayList<User>();
				List<User> participantList = new ArrayList<User>();
				
				conversationRoom.getCurrentMembers(); result = memberList;
				conversationRoom.getCurrentParticipants(); result = participantList;
				conversationRoom.getVirtualDeskUser(); times = 1;
				conversationRoom.getJID(); times = 3;
				
				MessageResponse.createMessageResponse((User)any, (Collection<User>)any, (JID)any, message); times = 2;
			}
		};
		
		memberMessageReceivedConversationHandler.handle();
	}

}
