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
import java.util.Arrays;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

public class ViewActiveConversationCommandTest
{
	private User adminUser01;
	private User adminUser02;
	private User memberUser01;
	private User ownerUser01;
	private User participantUser;
	private User participantUser2;
	
	private Desk deskRoom;
	
	private @Mocked @NonStrict DeskDirectory deskDirectory;
	
	private ViewActiveConversationCommand viewActiveConversationCommand;
	
	private String command = "@active";
	private String conversationAvailable = "Total active chat session: %d";
	private String conversationPerMember = "\r\n%s = %d";

	@Before
	public void before()
	{
		adminUser01 = UserCreator.createAdminUser("Admin.User01");
		adminUser02 = UserCreator.createAdminUser("Admin.User02");
		ownerUser01 = UserCreator.createOwnerUser("Owner.User01");
		memberUser01 = UserCreator.createMemberUser("Member.User01");
		participantUser = UserCreator.createParticipantUser("Participant.User01");
		participantUser2 = UserCreator.createParticipantUser("Participant.User02");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		
		deskRoom.addAdmin(adminUser01.getJID().toBareJID());
		deskRoom.addAdmin(adminUser02.getJID().toBareJID());
		deskRoom.addOwner(ownerUser01.getJID().toBareJID());
		deskRoom.addMember(memberUser01.getJID().toBareJID());
				
		viewActiveConversationCommand = new ViewActiveConversationCommand();
	}

	@Test
	public void canProcess_ParticipantOrMemberSendCommand()
	{	
		assertFalse(viewActiveConversationCommand.canProcess(command, memberUser01, deskRoom));
		assertFalse(viewActiveConversationCommand.canProcess(command, participantUser, deskRoom));
	}
	
	@Test
	public void canProcess_CommandIscaseinsensitive()
	{	
		command = "@ACTive";
		assertTrue(viewActiveConversationCommand.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void canProcess_CommandIncludingSpace()
	{
		command = " " + command + " ";
		assertTrue(viewActiveConversationCommand.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void canProcess_CommandIsEmptyString()
	{
		command = StringUtils.EMPTY;
		assertFalse(viewActiveConversationCommand.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void canProcess_CommandIsNotTheFirstWord()
	{
		command = "ABC " + command + " ";
		assertFalse(viewActiveConversationCommand.canProcess(command, adminUser01, deskRoom));
	}
	
	@Test
	public void process_CommandHasSubsequenceString_IgnoreSubSequence()
	{
		command = " " + command + " ";
		assertTrue(viewActiveConversationCommand.canProcess(command, adminUser01, deskRoom));
	}

	@Test
	public void internalProcess_NoActiveChat()
	{
		List<Packet> result = (List <Packet>) viewActiveConversationCommand.process(command, ownerUser01, deskRoom, deskDirectory);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof Message);
		assertTrue(((Message)result.get(0)).getBody().contains(String.format(conversationAvailable, 0)));
	}
	
	@Test
	public void internalProcess_activeChatWithOneToOne()
	{
		final Conversation conversation = new Conversation(deskRoom.getName()+"_1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		conversation.addOccupant(participantUser);
		conversation.addOccupant(adminUser01);
		
		deskRoom.addOccupant(participantUser);
		deskRoom.addOccupant(adminUser01);
		
		
		new Expectations()
		{
			{
				deskDirectory.getAllConversation(); result = Arrays.asList(conversation);
			}
		};
		
		viewActiveConversationCommand.internalProcess(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(conversationPerMember, adminUser01.getNickname().toLowerCase(), 1);
		
		assertEquals(1, viewActiveConversationCommand.packetList.size());
		assertTrue(viewActiveConversationCommand.packetList.get(0) instanceof Message);
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser.getNickname().toLowerCase()));
	}
	
	@Test
	public void internalProcess_adminHasManyChatActive()
	{
		final Conversation conversation1 = new Conversation(deskRoom.getName()+"_1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		conversation1.addOccupant(participantUser);
		conversation1.addOccupant(adminUser01);
		
		final Conversation conversation2 = new Conversation(deskRoom.getName()+"_5678", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		conversation2.addOccupant(participantUser2);
		conversation2.addOccupant(adminUser01);
		
		deskRoom.addOccupant(adminUser01);
		deskRoom.addOccupant(participantUser);
		deskRoom.addOccupant(participantUser2);
		
		new Expectations()
		{
			{
				deskDirectory.getAllConversation(); result = Arrays.asList(conversation1, conversation2);
			}
		};
		
		viewActiveConversationCommand.internalProcess(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(conversationPerMember, adminUser01.getNickname().toLowerCase(), 2);
		
		assertEquals(1, viewActiveConversationCommand.packetList.size());
		assertTrue(viewActiveConversationCommand.packetList.get(0) instanceof Message);
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser.getNickname().toLowerCase()));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser2.getNickname().toLowerCase()));
	}
	
	@Test
	public void internalProcess_ConversationHasNoMeberInChat()
	{
		final Conversation conversation1 = new Conversation(deskRoom.getName()+"_1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		
		new Expectations()
		{
			{
				deskDirectory.getAllConversation(); result = Arrays.asList(conversation1);
			}
		};
		
		viewActiveConversationCommand.internalProcess(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage = String.format(conversationAvailable, 0);

		assertTrue(viewActiveConversationCommand.packetList.get(0) instanceof Message);
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(ownerUser01.getNickname().toLowerCase()));
	}
	
	@Test
	public void internalProcess_MoreThanOneAdminAreChatting()
	{
		final Conversation conversation1 = new Conversation(deskRoom.getName()+"_1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		conversation1.addOccupant(participantUser);
		conversation1.addOccupant(adminUser01);
		
		final Conversation conversation2 = new Conversation(deskRoom.getName()+"_5678", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		conversation2.addOccupant(participantUser2);
		conversation2.addOccupant(adminUser02);
		
		deskRoom.addOccupant(participantUser);
		deskRoom.addOccupant(adminUser01);
		deskRoom.addOccupant(adminUser02);
		
		new Expectations()
		{
			{
				deskDirectory.getAllConversation(); result = Arrays.asList(conversation1, conversation2);
			}
		};
		
		viewActiveConversationCommand.internalProcess(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage1 = String.format(conversationPerMember, adminUser01.getNickname().toLowerCase(), 1);
		String expectedMessage2 = String.format(conversationPerMember, adminUser02.getNickname().toLowerCase(), 1);
		
		assertEquals(1, viewActiveConversationCommand.packetList.size());
		assertTrue(viewActiveConversationCommand.packetList.get(0) instanceof Message);
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage1));
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage2));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser.getNickname().toLowerCase()));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser2.getNickname().toLowerCase()));
	}
	
	@Test
	public void internalProcess_MoreThanOneAdminAreChatingAndOneAdminAreNotChating()
	{
		final Conversation conversation1 = new Conversation(deskRoom.getName()+"_1234", TestConst.VIRTUALDESK_DOMAIN, deskRoom);
		conversation1.addOccupant(participantUser);
		conversation1.addOccupant(adminUser01);
		
		deskRoom.addOccupant(adminUser01);
		deskRoom.addOccupant(adminUser02);
		
		new Expectations()
		{
			{
				deskDirectory.getAllConversation(); result = Arrays.asList(conversation1);
			}
		};
		
		viewActiveConversationCommand.internalProcess(command, ownerUser01, deskRoom, deskDirectory);
		
		String expectedMessage1 = String.format(conversationPerMember, adminUser01.getNickname().toLowerCase(), 1);
		String expectedMessage2 = String.format(conversationPerMember, adminUser02.getNickname().toLowerCase(), 0);
		
		assertEquals(1, viewActiveConversationCommand.packetList.size());
		assertTrue(viewActiveConversationCommand.packetList.get(0) instanceof Message);
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage1));
		assertTrue(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(expectedMessage2));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser.getNickname().toLowerCase()));
		assertFalse(((Message)viewActiveConversationCommand.packetList.get(0)).getBody().contains(participantUser2.getNickname().toLowerCase()));
	}

}
