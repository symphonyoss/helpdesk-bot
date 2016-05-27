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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.utils.PresenceSet;
import org.symphonyoss.collaboration.virtualdesk.utils.IQCreator;
import org.symphonyoss.collaboration.virtualdesk.utils.PresenceSet.ExtractPresence;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;


public class SetDeskFormHandler_OwnerTest
{

	private Desk deskRoom;
	private User roomowner;
	private IDeskDirectory deskDirectory;
	
	private String deskname = "Desk1";
	
	@Before
	public void setUp() throws IOException {
		
		deskDirectory = mock(DeskDirectory.class);
		
		roomowner = UserCreator.createOwnerUser("Owner.User01");

		deskRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		deskRoom.addOwner(roomowner.getJID().toBareJID());
		deskRoom.addOccupant(roomowner);
	}
	
	@Test
	public void ownerSetting_AddNewOwner_UserDoNotOnline()
	{
		User owner02 = UserCreator.createOwnerUser("owner.user02");
		
		//move admin to Owner
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(roomowner.getJID().toBareJID());
		updatedRoom.addOwner(owner02.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();

		assertNotNull(result);
		assertEquals(1, result.size());
		
		checkIQResult(result.get(0));
	}
	
	@Test
	public void ownerSetting_RemoveOwner_RemoveItself()
	{
		User owner02 = UserCreator.createOwnerUser("owner.user02");
		deskRoom.addOwner(owner02.getJID().toBareJID());
		
		//remove itself from desk owner
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(owner02.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();
		//System.out.println(result);
		assertNotNull(result);
		checkIQResult(result.get(0));
		PresenceSet extactPresence = checkPresenceCount(5, result);
		
		assertEquals(4, extactPresence.getPacketCount(roomowner.getJID()));
		assertEquals(0, extactPresence.getPacketCount(owner02.getJID()));
		
		roomowner.setAffiliation(Affiliation.none);
		roomowner.setRole(Role.participant);
		
		checkPacket(roomowner, extactPresence.getPresences(roomowner.getJID()));
	}
	
	@Test
	public void ownerSetting_UpdateAdminToOwner()
	{
		User adminUser01 = UserCreator.createAdminUser("Admin.User01");
		
		//1 admin joining the room
		deskRoom.addOccupant(adminUser01);
		
		//move admin to Owner
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(roomowner.getJID().toBareJID());
		updatedRoom.addOwner(adminUser01.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();
		
		assertNotNull(result);
		assertEquals(1, result.size());
		checkIQResult(result.get(0));
	}
	
	@Test
	public void ownerSetting_UpdateParticipantToOwner()
	{
		User participantUser01 = UserCreator.createParticipantUser("Participant.User01");
		
		//1 participant joining the room
		deskRoom.addOccupant(participantUser01);
		
		//move participant to Owner
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(roomowner.getJID().toBareJID());
		updatedRoom.addOwner(participantUser01.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();

		assertNotNull(result);
		checkIQResult(result.get(0));
		PresenceSet extactPresence = checkPresenceCount(6, result);
		
		assertEquals(1, extactPresence.getPacketCount(roomowner.getJID()));
		assertEquals(4, extactPresence.getPacketCount(participantUser01.getJID()));
		
		participantUser01.setAffiliation(Affiliation.owner);
		participantUser01.setRole(Role.moderator);
		
		checkPacket(participantUser01, extactPresence.getPresences(roomowner.getJID()));
	}
	
	@Test
	public void ownerSetting_UpdateOwnerToAdmin()
	{
		User owner02 = UserCreator.createOwnerUser("owner.User02");
		
		//Another owner joining the room
		deskRoom.addOccupant(owner02);
		
		//move owner to admin
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(roomowner.getJID().toBareJID());
		updatedRoom.addAdmin(owner02.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();
		
		assertNotNull(result);
		assertEquals(1, result.size());
		checkIQResult(result.get(0));
	}
	
	@Test
	public void ownerSetting_UpdateOwnerToParticipant()
	{
		User owner02 = UserCreator.createOwnerUser("owner.User02");
		
		//Another owner joining the room
		deskRoom.addOccupant(owner02);
		
		//remove owner
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(roomowner.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();
		
		assertNotNull(result);
		checkIQResult(result.get(0));
		PresenceSet extactPresence = checkPresenceCount(6, result);
		
		assertEquals(1, extactPresence.getPacketCount(roomowner.getJID()));
		assertEquals(4, extactPresence.getPacketCount(owner02.getJID()));
		
		owner02.setAffiliation(Affiliation.none);
		owner02.setRole(Role.participant);
		
		checkPacket(owner02, extactPresence.getPresences(roomowner.getJID()));
	}
	
	@Test
	public void ownerSetting_UpdateOwnerToParticipant_OtherPartipantAndAdminOnline()
	{
		User participant01 = UserCreator.createParticipantUser("Participant.User01");
		User admin01 = UserCreator.createAdminUser("admin.User01");
		User owner02 = UserCreator.createOwnerUser("owner.User02");
		
		//Another owner, admin and participant joining the room
		deskRoom.addOccupant(participant01);
		deskRoom.addOccupant(admin01);
		deskRoom.addOccupant(owner02);
		deskRoom.addOwner(owner02.getJID().toBareJID());
		deskRoom.addAdmin(admin01.getJID().toBareJID());
		
		//remove owner
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		updatedRoom.addOwner(roomowner.getJID().toBareJID());
		updatedRoom.addAdmin(admin01.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();
		System.out.println(result);
		assertNotNull(result);
		checkIQResult(result.get(0));
		PresenceSet extactPresence = checkPresenceCount(9, result);
		
		assertEquals(1, extactPresence.getPacketCount(roomowner.getJID()));
		assertEquals(6, extactPresence.getPacketCount(owner02.getJID()));
		assertEquals(1, extactPresence.getPacketCount(admin01.getJID()));
		assertEquals(0, extactPresence.getPacketCount(participant01.getJID()));
		
		owner02.setAffiliation(Affiliation.none);
		owner02.setRole(Role.participant);
		
		checkPacket(owner02, extactPresence.getPresences(roomowner.getJID()));
		checkPacket(owner02, extactPresence.getPresences(admin01.getJID()));
	}
	
	@Test
	public void ownerSetting_AddNoOwner()
	{
		Desk updatedRoom = new Desk(deskname, TestConst.VIRTUALDESK_DOMAIN);
		
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, updatedRoom), deskRoom, deskDirectory).handle();
		
		PacketError errorPacket = result.get(0).getError();
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof IQ);
		assertEquals(deskRoom.getJID(), result.get(0).getFrom());
		assertEquals(roomowner.getJID(), result.get(0).getTo());
		assertEquals(PacketError.Type.cancel, errorPacket.getType());
	}
	
	@Test
	public void ownerSetting_UserHaveNoPermission()
	{
		User owner02 = UserCreator.createOwnerUser("owner.User02");
		
		deskRoom.syncOwners(new HashSet <String>(Arrays.asList(owner02.getJID().toBareJID())));
		List <Packet> result = (List <Packet>) new SetDeskFormHandler(IQCreator.createIQResponseRoomSetting(roomowner, deskRoom), deskRoom, deskDirectory).handle();
		
		PacketError errorPacket = result.get(0).getError();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.get(0) instanceof IQ);
		assertEquals(deskRoom.getJID(), result.get(0).getFrom());
		assertEquals(roomowner.getJID(), result.get(0).getTo());
		assertEquals(PacketError.Type.cancel, errorPacket.getType());
		assertEquals(PacketError.Condition.not_allowed, errorPacket.getCondition());
	}
	
	private void checkPacket(User user, List<ExtractPresence> list)
	{
		for(ExtractPresence item : list)
		{
			if(item.type == Presence.Type.unavailable)
				continue;
			
			if(StringUtils.isNotEmpty(item.nickName) && item.nickName.endsWith(user.getNickname()))
			{
				assertEquals(user.getAffiliation(), item.affiliation);
				assertEquals(user.getRole(), item.role);
			}
		}
	}
	
	private void checkIQResult(Packet resultIQ )
	{
		assertTrue(resultIQ instanceof IQ);
		assertEquals(IQ.Type.result, ((IQ)resultIQ).getType());	
		assertEquals(deskRoom.getJID(), resultIQ.getFrom());
		assertEquals(roomowner.getJID(), resultIQ.getTo());		
	}
	
	private PresenceSet checkPresenceCount(int expectedCount, List <Packet> result)
	{
		PresenceSet extactPresence = new PresenceSet();
		assertEquals(expectedCount, result.size());
		
		for(int i=1; i< expectedCount; i++){
			Packet item = result.get(i);
			assertTrue(item instanceof Presence);
			extactPresence.addPresence((Presence)item);
		}
		
		return extactPresence;
	}
}
