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

import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.symphonyoss.collaboration.virtualdesk.command.AbstractCommand;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class ParticipantMessageReceivedHandlerTest
{
	private ParticipantMessageReceivedHandler messageReceivedHandler;
	
	private Message message;
	
	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	
	private @Mocked @NonStrict Desk deskRoom;
	
	private @Mocked @NonStrict MessageResponse messageResponse;
	
	private @Mocked @NonStrict IDeskDirectory deskDirectory;


	
	@Before
	public void before()
	{
		message = new Message();
		
		message.setFrom(JIDUtils.getUserJID("user1"));
		message.setTo(JIDUtils.getNicknameJID("desk1", null));
		message.setBody("test");

		messageReceivedHandler = new ParticipantMessageReceivedHandler(serviceConfiguration, message, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_UserNeverPostQuestionBefore_AddNewQuestion()
	{
		new Expectations()
		{
			{
				User senderUser = new User(JIDUtils.getUserJID("user1"), "user1");
				
				deskRoom.getOccupantByJID((JID)any); result = senderUser;
				
				deskRoom.getQuestion(anyString); result = null;
				
				deskRoom.addNewQuestion(senderUser.getNickname(), senderUser.getJID().toString(), anyString); times = 1;
			}
		};
		
		messageReceivedHandler.handle();
	}
	
	@Test
	public void handle_UserAlreadyPostedQuestionAndPostAgain_AppendToExistingQuestion()
	{
		final UserState userState = new UserState("user2", JIDUtils.getUserBareJID("user2"), "question1");
		userState.setState(WorkflowState.AwaitResponse);
		
		new Expectations()
		{
			{
				User senderUser = new User(JIDUtils.getUserJID("user2"), "user2");
				
				deskRoom.getOccupantByJID((JID)any); result = senderUser;
				
				deskRoom.getQuestion(anyString); result = userState;
			}
		};
		
		messageReceivedHandler.handle();
		
		Assert.assertEquals(2, userState.getQuestions().size());
		Assert.assertEquals(message.getBody(), ((List<String>)userState.getQuestions()).get(1));
	}
	
	@Test
	public void handle_UserIsInPrivateConversation_ResponseMessageToSender()
	{
		final UserState userState = new UserState("user2", JIDUtils.getUserBareJID("user2"), "question1");
		userState.setState(WorkflowState.InConversation);
		
		new Expectations()
		{
			{
				User senderUser = new User(JIDUtils.getUserJID("user2"), "user2");
				
				deskRoom.getOccupantByJID((JID)any); result = senderUser;
				
				deskRoom.getQuestion(anyString); result = userState;
				
				MessageResponse.createDeskMessageResponse(anyString, senderUser, (JID)any); times = 1;
			}
		};
		
		messageReceivedHandler.handle();
	}
	
	@Test
	public void handle_UserSendCommandMessage_CallCommand()
	{
		message.setBody("@test");
		
		new Expectations()
		{
			@Mocked @NonStrict AbstractCommand command;
			{
				// Response message to sender
				MessageResponse.createMessageResponse((User)any, (User)any, (JID)any, (Message)any); times = 1;
				
				command.process(anyString, (User)any, (Desk)any, (IDeskDirectory)any); times = 1;
			}
		};
		
		messageReceivedHandler.handle();
	}
}
