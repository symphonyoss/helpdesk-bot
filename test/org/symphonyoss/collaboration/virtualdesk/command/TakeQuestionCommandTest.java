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

package org.symphonyoss.collaboration.virtualdesk.command;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPersistenceManager;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class TakeQuestionCommandTest
{
	private User adminUser;
	private User participantUser;
	private Desk deskRoom;


	private @Mocked @NonStrict DeskDirectory deskDirectory;
	private @Mocked @NonStrict DeskPersistenceManager deskPersistenceManager;
	
	private TakeQuestionCommand takeQuestionCommand;

	@Before
	public void before()
	{
		adminUser = UserCreator.createOwnerUser("Admin.User01");

		participantUser = UserCreator.createParticipantUser("Part.User01");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		deskRoom.setDeskPersistenceManager(deskPersistenceManager);

		takeQuestionCommand = new TakeQuestionCommand();
	}

	@Test
	public void process_CommandIsNotTakeQuestionCommand_CallToNextCommand()
	{
		String command = "@abc";

		AbstractCommand mockCommand = mock(AbstractCommand.class);
		takeQuestionCommand.setNext(mockCommand);

		takeQuestionCommand.process(command, adminUser, deskRoom, null);

		verify(mockCommand).process(command, adminUser, deskRoom, null);
	}

	@Test
	public void process_UserIsParticipant_CallToNextCommand()
	{
		AbstractCommand mockCommand = mock(AbstractCommand.class);
		takeQuestionCommand.setNext(mockCommand);

		takeQuestionCommand.process("@take", participantUser, deskRoom, null);

		verify(mockCommand).process("@take", participantUser, deskRoom, null);
	}

	@Test
	public void process_CommandHasNoArgument_NoPacketReturn()
	{
		List <Packet>  packetList = (List <Packet>)takeQuestionCommand.process("@take", adminUser, deskRoom, null);

		Assert.assertEquals(1, packetList.size());
		
		Assert.assertTrue(packetList.get(0) instanceof Message);
		Assert.assertEquals(((Message)packetList.get(0)).getBody() , "@take <nickname>");
	}

	@Test
	public void process_CommandHasArgumentMoreThanTwo_NoPacketReturn()
	{
		List <Packet> packetList = (List <Packet>)takeQuestionCommand.process("@take abc efg", adminUser, deskRoom, null);

		Assert.assertEquals(1, packetList.size());
		Assert.assertTrue(packetList.get(0) instanceof Message);
		Assert.assertEquals(((Message)packetList.get(0)).getBody() , String.format("No question of %s is posted in Desk.", "abc efg"));
	}

	@Test
	public void process_NoQuestionOfTakenUser_ReturnMessageToSender()
	{
		List <Packet> packetList = (List <Packet>) takeQuestionCommand.process("@take abc", adminUser, deskRoom, null);

		Assert.assertEquals(1, packetList.size());
		Assert.assertTrue(packetList.get(0) instanceof Message);
		Assert.assertEquals(deskRoom.getJID(), packetList.get(0).getFrom());
		Assert.assertEquals(adminUser.getJID(), packetList.get(0).getTo());
	}

	@Test
	public void process_TakenUserAlreadyPostedQuestion_UserStateIsChangedToInConversation()
	{
		deskRoom.addNewQuestion("user1", JIDUtils.getUserBareJID("user1"), "What is the price today?");
		
		final Conversation conversation = new Conversation(deskRoom.getName() + "1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		
		new Expectations()
		{
			{
				deskDirectory.createDeskConversation(anyString, anyString, deskRoom); result = conversation;
			}
		};
		
		takeQuestionCommand.process("@take user1", adminUser, deskRoom, deskDirectory);

		UserState state = deskRoom.getQuestion("user1");

		Assert.assertEquals(WorkflowState.InConversation, state.getState());
	}
	
	@Test
	public void process_TakeUserWithCaseInsensitiveNickname_UserStateIsChangedToInConversation()
	{
		deskRoom.addNewQuestion("user1", JIDUtils.getUserBareJID("user1"), "What is the price today?");
		
		final Conversation conversation = new Conversation(deskRoom.getName() + "1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		
		new Expectations()
		{
			{
				deskDirectory.createDeskConversation(anyString, anyString, deskRoom); result = conversation;
			}
		};
		
		takeQuestionCommand.process("@take  USEr1 ", adminUser, deskRoom, deskDirectory);

		UserState state = deskRoom.getQuestion("user1");

		Assert.assertEquals(WorkflowState.InConversation, state.getState());
	}

	@Test
	public void process_QuestionHasAlreadyTaken_ReturnMessageToSender()
	{
		deskRoom.addNewQuestion("user1", JIDUtils.getUserBareJID("user1"), "What is the price today?");

		UserState state = deskRoom.getQuestion("user1");

		// Fake the state of user to be in conversation with broker
		state.setState(WorkflowState.InConversation);

		List <Packet> packetList = (List <Packet>) takeQuestionCommand.process("@take user1", adminUser, deskRoom, null);

		Assert.assertEquals(1, packetList.size());
		Assert.assertTrue(packetList.get(0) instanceof Message);
		Assert.assertEquals(deskRoom.getJID(), packetList.get(0).getFrom());
		Assert.assertEquals(adminUser.getJID(), packetList.get(0).getTo());
	}
}
