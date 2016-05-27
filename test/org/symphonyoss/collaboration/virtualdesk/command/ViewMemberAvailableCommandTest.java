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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class ViewMemberAvailableCommandTest
{
	private User adminUser01;
	private User adminUser02;
	private User memberUser01;
	private User ownerUser01;
	private User participantUser;
	
	private Desk deskRoom;
	
	private AbstractCommand abstractCommand;
	
	private DeskDirectory deskDirectory;
	
	private ViewMemberAvailableCommand viewMemberAvailableCmd;
	
	private String command = "@members";
	private String noMemberAvailable = "No member is available";
	private String memberAvailable = "There are %s in Desk.";

	@Before
	public void before()
	{
		adminUser01 = UserCreator.createAdminUser("Admin.User01");
		adminUser02 = UserCreator.createAdminUser("Admin.User02");
		ownerUser01 = UserCreator.createOwnerUser("Owner.User01");
		memberUser01 = UserCreator.createMemberUser("Member.User01");
		participantUser = UserCreator.createParticipantUser("Participant.User01");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		
		deskRoom.addAdmin(adminUser01.getJID().toBareJID());
		deskRoom.addAdmin(adminUser02.getJID().toBareJID());
		deskRoom.addOwner(ownerUser01.getJID().toBareJID());
		deskRoom.addMember(memberUser01.getJID().toBareJID());
		deskRoom.addParticipants(participantUser.getJID().toBareJID());
		
		deskDirectory = mock(DeskDirectory.class); 
		
		viewMemberAvailableCmd = new ViewMemberAvailableCommand();
	}
	
	@Test
	public void canProcess_ParticipantOrMemberSendCommand()
	{	
		assertFalse(viewMemberAvailableCmd.canProcess(command, memberUser01, deskRoom));
		assertFalse(viewMemberAvailableCmd.canProcess(command, participantUser, deskRoom));
	}
	
	@Test
	public void canProcess_CommandIscaseinsensitive()
	{	
		command = "@MEMBers";
		assertTrue(viewMemberAvailableCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandIncludingSpace()
	{
		command = " " + command + " ";
		assertTrue(viewMemberAvailableCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandIsEmptyString()
	{
		command = StringUtils.EMPTY;
		assertFalse(viewMemberAvailableCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandHasSubsequenceString_IgnoreSubSequence()
	{
		command += " abc";
		assertTrue(viewMemberAvailableCmd.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandIsNotTheFirstWord()
	{
		command = " abc" + command;
		assertFalse(viewMemberAvailableCmd.canProcess(command, adminUser01, deskRoom));
	}

	@Test
	public void process_NotCheckMemberCommand_CallNextCommand()
	{
		abstractCommand = mock(AbstractCommand.class);
		viewMemberAvailableCmd.setNext(abstractCommand);
		
		viewMemberAvailableCmd.process("@command", adminUser01, deskRoom, null);

		verify(abstractCommand).process("@command", adminUser01, deskRoom, null);
	}

	@Test
	public void internalProcess_OneMemberOnline()
	{
		deskRoom.addOccupant(ownerUser01);
		List<Packet> result = (List <Packet>) viewMemberAvailableCmd.process(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(memberAvailable, 1);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(expectedMessage));
	}
	
	@Test
	public void internalProcess_NoMemberOnline()
	{
		deskRoom.addOccupant(ownerUser01);
		ownerUser01.setPresence(PresenceType.Away);
		
		List<Packet> result = (List <Packet>) viewMemberAvailableCmd.process(command, ownerUser01, deskRoom, deskDirectory);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(noMemberAvailable));
	}
	
	@Test
	public void internalProcess_OneAdminIsFreeToChat()
	{
		deskRoom.addOccupant(ownerUser01);
		deskRoom.addOccupant(adminUser01);
		deskRoom.addOccupant(adminUser02);
		ownerUser01.setPresence(PresenceType.Away);
		adminUser02.setPresence(PresenceType.DoNotDisturb);
		adminUser01.setPresence(PresenceType.FreeToChat);
		
		List<Packet> result = (List <Packet>) viewMemberAvailableCmd.process(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(memberAvailable, 1);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(expectedMessage));
		assertTrue(((Message)result.get(0)).getBody().contains(adminUser01.getNickname()));
		assertFalse(((Message)result.get(0)).getBody().contains(adminUser02.getNickname()));
		assertFalse(((Message)result.get(0)).getBody().contains(ownerUser01.getNickname()));
	}	

}
