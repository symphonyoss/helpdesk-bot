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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskNonPersistenceManager;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

public class ViewPostedMessageInQueueCommandTest
{
	private User adminUser01;
	private User adminUser02;
	private User memberUser01;
	private User ownerUser01;
	private User participantUser;
	
	private Desk deskRoom;
	
	private AbstractCommand abstractCommand;
	
	private DeskDirectory deskDirectory;
	
	private ViewPostedMessageInQueueCommand viewPostedMessageQueueCmd;
	
	private String command = "@queue";
	private String noMessageInQueue = "No posted message in queue";
	private String messageInQueue = "There are %s posted message in queue.";

	@Before
	public void before()
	{
		adminUser01 = UserCreator.createAdminUser("Admin.User01");
		adminUser02 = UserCreator.createAdminUser("Admin.User02");
		ownerUser01 = UserCreator.createOwnerUser("Owner.User01");
		memberUser01 = UserCreator.createMemberUser("Member.User01");
		participantUser = UserCreator.createParticipantUser("Participant.User01");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		deskRoom.setDeskPersistenceManager(new DeskNonPersistenceManager(null));
		
		deskRoom.addAdmin(adminUser01.getJID().toBareJID());
		deskRoom.addAdmin(adminUser02.getJID().toBareJID());
		deskRoom.addOwner(ownerUser01.getJID().toBareJID());
		deskRoom.addMember(memberUser01.getJID().toBareJID());
		deskRoom.addParticipants(participantUser.getJID().toBareJID());

		deskDirectory = mock(DeskDirectory.class); 
				
		viewPostedMessageQueueCmd = new ViewPostedMessageInQueueCommand();
	}

	@Test
	public void process_NotQueueCommand_CallNextCommand()
	{
		abstractCommand = mock(AbstractCommand.class);
		viewPostedMessageQueueCmd.setNext(abstractCommand);
		
		viewPostedMessageQueueCmd.process("@command", adminUser01, deskRoom, null);

		verify(abstractCommand).process("@command", adminUser01, deskRoom, null);
	}
	
	@Test
	public void canProcess_ParticipantOrMemberSendCommand()
	{	
		assertTrue(viewPostedMessageQueueCmd.canProcess(command, memberUser01, deskRoom));
		assertFalse(viewPostedMessageQueueCmd.canProcess(command, participantUser, deskRoom));
	}
	
	@Test
	public void canProcess_CommandIscaseinsensitive()
	{	
		command = "@queUE";
		assertTrue(viewPostedMessageQueueCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandIncludingSpace()
	{
		command = " " + command + " ";
		assertTrue(viewPostedMessageQueueCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandIsEmptyString()
	{
		command = StringUtils.EMPTY;
		assertFalse(viewPostedMessageQueueCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandHasSubsequenceString_IgnoreSubSequence()
	{
		command += " abc";
		assertTrue(viewPostedMessageQueueCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandIsNotTheFirstWord()
	{
		command = " abc " + command;
		assertFalse(viewPostedMessageQueueCmd.canProcess(command, adminUser01, deskRoom));
	}

	@Test
	public void internalProcess_NoMessageInQueue()
	{
		List<Packet> result = (List <Packet>) viewPostedMessageQueueCmd.process(command, ownerUser01, deskRoom, deskDirectory);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(noMessageInQueue));
	}
	
	@Test
	public void internalProcess_MessageAwaitResponse_ReturnMessageResponse()
	{
		deskRoom.addNewQuestion(participantUser.getNickname(), participantUser.getJID().toString(), "Hi how are you");
		deskRoom.addNewQuestion(memberUser01.getNickname(), memberUser01.getJID().toString(), "Hello, What's up?");
		
		List<Packet> result = (List <Packet>) viewPostedMessageQueueCmd.process(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(messageInQueue, 2);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(expectedMessage));
		assertTrue(((Message)result.get(0)).getBody().contains(participantUser.getNickname()));
		assertTrue(((Message)result.get(0)).getBody().contains(memberUser01.getNickname()));
	}
	
	@Test
	public void internalProcess_MessageInConversation_ReturnNoMessageResponse()
	{
		deskRoom.addNewQuestion(participantUser.getNickname(), participantUser.getJID().toString(), "Hi, how are you?");
		deskRoom.addNewQuestion(memberUser01.getNickname(), memberUser01.getJID().toString(), "Hello, What's up?");
		
		UserState userState = deskRoom.getQuestion(participantUser.getNickname());
		userState.setState(WorkflowState.InConversation);
		
		List<Packet> result = (List <Packet>) viewPostedMessageQueueCmd.process(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(messageInQueue, 1);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(expectedMessage));
		assertTrue(((Message)result.get(0)).getBody().contains(memberUser01.getNickname()));
		assertFalse(((Message)result.get(0)).getBody().contains(participantUser.getNickname()));
	}
	
}
