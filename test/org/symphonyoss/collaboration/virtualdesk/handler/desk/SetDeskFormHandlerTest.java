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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.utils.IQCreator;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;


public class SetDeskFormHandlerTest
{

	private Desk deskRoom;
	private User roomowner;
	private IDeskDirectory deskDirectory;
	
	@Before
	public void setUp() throws IOException {
		
		deskDirectory = mock(DeskDirectory.class);
		
		roomowner = UserCreator.createOwnerUser("Owner.User01");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		deskRoom.addOwner(roomowner.getJID().toBareJID());
		deskRoom.addOccupant(roomowner);
	}

	@Test
	public void adminSetting_UpdateRoomSingleProperty()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.setDescription("Desk1_Description");
		updatedDesk.isMembersOnly(true);

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();

		assertEquals(updatedDesk.getDescription(), deskRoom.getDescription());
		assertEquals(updatedDesk.isMembersOnly(), deskRoom.isMembersOnly());
		assertEquals(updatedDesk.getName(), deskRoom.getName());
		assertEquals(updatedDesk.getSubject(), deskRoom.getSubject());
	}
	
	@Test
	public void adminSetting_AddNewAdmin()
	{
		User Admin01 = UserCreator.createAdminUser("Admin.User01");
		User Admin02 = UserCreator.createAdminUser("Admin.User02");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addAdmin(Admin01.getJID().toBareJID());
		updatedDesk.addAdmin(Admin02.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(2, deskRoom.getAdmins().size());
		assertTrue(deskRoom.isAdmin(Admin01.getJID().toBareJID()));
		assertTrue(deskRoom.isAdmin(Admin02.getJID().toBareJID()));
	}
	
	@Test
	public void adminSetting_RemoveAdmin()
	{
		User Admin01 = UserCreator.createAdminUser("Admin.User01");
		User Admin02 = UserCreator.createAdminUser("Admin.User02");
		
		deskRoom.addAdmin(Admin01.getJID().toBareJID());
		deskRoom.addAdmin(Admin02.getJID().toBareJID());
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addAdmin(Admin01.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(1, deskRoom.getAdmins().size());
		assertTrue(deskRoom.isAdmin(Admin01.getJID().toBareJID()));
		assertFalse(deskRoom.isAdmin(Admin02.getJID().toBareJID()));
	}
	
	@Test
	public void adminSetting_AddNewAdmin_DuplicateUser()
	{
		User Admin01 = UserCreator.createAdminUser("Admin.User01");
		User Admin02 = UserCreator.createAdminUser("Admin.User02");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addAdmin(Admin01.getJID().toBareJID());
		updatedDesk.addAdmin(Admin01.getJID().toBareJID());
		updatedDesk.addAdmin(Admin02.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(2, deskRoom.getAdmins().size());
		assertTrue(deskRoom.isAdmin(Admin01.getJID().toBareJID()));
		assertTrue(deskRoom.isAdmin(Admin02.getJID().toBareJID()));
	}	
	
	@Test
	public void adminSetting_AddNewAdmin_InvalidJID()
	{		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addAdmin("abc");

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(0, deskRoom.getAdmins().size());
		assertFalse(deskRoom.isAdmin("abc"));
	}
	
	@Test
	public void adminSetting_AddNewAdmin_JIDIncludingComma()
	{		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addAdmin("abc@test,de");

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(0, deskRoom.getAdmins().size());
		assertFalse(deskRoom.isAdmin("abc@test,de"));
	}
	
	@Test
	public void adminSetting_AddNewOwner()
	{
		User owner02 = UserCreator.createOwnerUser("Owner.User02");
		User owner03 = UserCreator.createOwnerUser("Owner.User03");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addOwner(owner02.getJID().toBareJID());
		updatedDesk.addOwner(owner03.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(3, deskRoom.getOwners().size());
		assertTrue(deskRoom.isOwner(owner02.getJID().toBareJID()));
		assertTrue(deskRoom.isOwner(owner03.getJID().toBareJID()));
	}
	
	@Test
	public void adminSetting_RemoveOwner()
	{
		User owner02 = UserCreator.createOwnerUser("Owner.User02");
		User owner03 = UserCreator.createOwnerUser("Owner.User03");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);
		
		updatedDesk.addOwner(owner02.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(2, deskRoom.getOwners().size());
		assertTrue(deskRoom.isOwner(owner02.getJID().toBareJID()));
		assertFalse(deskRoom.isOwner(owner03.getJID().toBareJID()));
	}
	
	@Test
	public void adminSetting_AddNewMember()
	{
		User user01 = UserCreator.createMemberUser("User.User01");
		User user02 = UserCreator.createMemberUser("User.User02");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);
		
		updatedDesk.addMember(user01.getJID().toBareJID());
		updatedDesk.addMember(user02.getJID().toBareJID());
		
		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(2, deskRoom.getMembers().size());
		assertTrue(deskRoom.isMember(user01.getJID().toBareJID()));
		assertTrue(deskRoom.isMember(user02.getJID().toBareJID()));
	}
	
	@Test
	public void adminSetting_AddNewParticipant()
	{
		User user01 = UserCreator.createParticipantUser("User.User01");
		User user02 = UserCreator.createParticipantUser("User.User02");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addParticipants(user01.getJID().toBareJID());
		updatedDesk.addParticipants(user02.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(2, deskRoom.getParticipants().size());
		assertTrue(deskRoom.isMemberParticipant(user01.getJID().toBareJID()));
		assertTrue(deskRoom.isMemberParticipant(user02.getJID().toBareJID()));
	}
	
	@Test
	public void adminSetting_RemoveParticipant()
	{
		User user01 = UserCreator.createParticipantUser("User.User01");
		User user02 = UserCreator.createParticipantUser("User.User02");
		
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);

		updatedDesk.addParticipants(user01.getJID().toBareJID());

		new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		assertEquals(1, deskRoom.getParticipants().size());
		assertTrue(deskRoom.isMemberParticipant(user01.getJID().toBareJID()));
		assertFalse(deskRoom.isMemberParticipant(user02.getJID().toBareJID()));
	}
	
	@Test
	public void handle_RemoveAllOwners_ResponseErrorBack()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		
		SetDeskFormHandler handler = new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory);
		List<Packet> packetList = (List<Packet>)handler.handle();
		
		Assert.assertEquals(1, packetList.size());
		Assert.assertEquals(PacketError.Condition.not_acceptable, ((IQ)packetList.get(0)).getError().getCondition());
	}
	
	@Test
	public void handle_updateAdmin_RoomPresenceChanged()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.addOccupant(roomowner);
		
		roomowner.setPresence(PresenceType.DoNotDisturb);
		
		User user01 = UserCreator.createParticipantUser("User.User01");
		User user02 = UserCreator.createParticipantUser("User.User02");
		
		user01.setPresence(PresenceType.Online);
		updatedDesk.addAdmin(user01.getJID().toBareJID());
		updatedDesk.addParticipants(user02.getJID().toBareJID());
		
		deskRoom.addOccupant(user01);
		deskRoom.addOccupant(user02);
		
		List<Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory).handle();
		
		Presence newPresence = (Presence) result.get(result.size() - 1);
		assertEquals(PresenceType.Online.toString(), newPresence.getStatus());
		assertTrue(deskRoom.isAdmin(user01.getJID().toBareJID()));
	}

	@Test
	public void handle_UpdateDeskAliasWithWhitespaces_ResponseIQError()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.setDeskAliasName("\20\20");
		
		SetDeskFormHandler handler = new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory);
		List<Packet> packetList = (List<Packet>)handler.handle();
		
		Assert.assertEquals(1, packetList.size());
		Assert.assertEquals(PacketError.Condition.not_acceptable, ((IQ)packetList.get(0)).getError().getCondition());
	}

	@Test
	public void handle_UpdateDeskAliasWithValidNormalName_DeskNameIsChanged()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.setDeskAliasName("NewDeskName");
		
		SetDeskFormHandler handler = new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory);
		handler.handle();
		
		Assert.assertEquals(updatedDesk.getDeskAliasName(), deskRoom.getDeskAliasName());
	}
	
	@Test
	public void handle_UpdateDeskAliasWithValidEscapedName_DeskNameIsChanged()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.setDeskAliasName("S\40P");
		
		SetDeskFormHandler handler = new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory);
		handler.handle();
		
		Assert.assertEquals(JID.unescapeNode(updatedDesk.getDeskAliasName()), deskRoom.getDeskAliasName());
	}
	
	@Test
	public void handle_UpdateDeskAliasThatAlreadyExisted_ResponseIQError()
	{
		Desk updatedDesk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		updatedDesk.addOwner(roomowner.getJID().toBareJID());
		updatedDesk.setDeskAliasName(roomowner.getNickname());
		
		SetDeskFormHandler handler = new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedDesk), deskRoom, deskDirectory);
		List<Packet> packetList = (List<Packet>)handler.handle();
		
		Assert.assertEquals(1, packetList.size());
		Assert.assertEquals(PacketError.Condition.not_acceptable, ((IQ)packetList.get(0)).getError().getCondition());
	}
}
