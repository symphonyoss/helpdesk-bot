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

import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.utils.Utils;
import static org.junit.Assert.*;

/**
 * To handle user change presence test
 * 
 * Test Idea
 * - User change presence with no admin
 * - User change presence with admin exists
 * - User change presence with user more than one
 * - User change presence with many user and admin
 * - Admin change presence with no user
 * - 1 Admins change presence with more nagative
 * - 1 Admins change presence with more positive
 */
public class PresenceChangedHandlerTest
{
	private Desk deskRoom;
	private User sender;
	private String deskName = "room1";
	private String deskDomain = "localhost";
	
	@Before
	public void setUp() throws IOException {
		deskRoom = new Desk(deskName, deskDomain);
	}

	/**
	 * Stil do not assert packet attribute 
	 * 
	 * 
	 */
	
	@Test
	public void handleUserPresence_NoAdmin()
	{
		sender = Utils.createUser("sender", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(sender);
		
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void handleUserPresence_AdminExists()
	{
		sender = Utils.createUser("sender", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(sender);
		
		User admin1 = Utils.createUser("Admin1", Affiliation.admin, Role.moderator, PresenceType.Online);
		deskRoom.addOccupant(admin1);
		
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(2, result.size());
	}
	
	@Test
	public void handleUserPresence_ManyUser()
	{
		sender = Utils.createUser("sender", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(sender);
		
		User admin1 = Utils.createUser("Admin1", Affiliation.admin, Role.moderator, PresenceType.Online);
		User user1 = Utils.createUser("User1", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(user1);
		deskRoom.addOccupant(admin1);
		
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(2, result.size());
	}
	
	@Test
	public void handleUserPresence_ManyUserManyAdmin()
	{
		sender = Utils.createUser("sender", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(sender);
		
		User admin1 = Utils.createUser("Admin1", Affiliation.admin, Role.moderator, PresenceType.Online);
		User admin2 = Utils.createUser("Admin2", Affiliation.admin, Role.moderator, PresenceType.Online);
		User user1 = Utils.createUser("User1", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(user1);
		deskRoom.addOccupant(admin1);
		deskRoom.addOccupant(admin2);
		
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
	@Test
	public void handleAdminPresence_NoUser()
	{
		sender = Utils.createUser("sender", Affiliation.admin, Role.moderator, PresenceType.Online);
		deskRoom.addOccupant(sender);
		
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void handleAdminPresence_OnlineToAway()
	{
		sender = Utils.createUser("sender", Affiliation.admin, Role.moderator, PresenceType.Online);
		User admin1 = Utils.createUser("admin1", Affiliation.admin, Role.moderator, PresenceType.Online);
		User user1 = Utils.createUser("user1", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(sender);
		deskRoom.addOccupant(admin1);
		deskRoom.addOccupant(user1);
		
		sender.setPresence(PresenceType.Away);
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
	@Test
	public void handleAdminPresence_AwayToOnline()
	{
		sender = Utils.createUser("sender", Affiliation.admin, Role.moderator, PresenceType.Away);
		User admin1 = Utils.createUser("admin1", Affiliation.admin, Role.moderator, PresenceType.Online);
		User user1 = Utils.createUser("user1", Affiliation.none, Role.participant, PresenceType.Online);
		deskRoom.addOccupant(sender);
		deskRoom.addOccupant(admin1);
		deskRoom.addOccupant(user1);
		
		sender.setPresence(PresenceType.Online);
		Presence presence = createPresence(sender);
		
		PresenceChangedHandler handler = new PresenceChangedHandler(presence, deskRoom);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
	//Utilities function
	private Presence createPresence(User sender)
	{
		Presence presence = new Presence();
		presence.setFrom(sender.getJID());
		presence.setTo(deskRoom.getJID());
		
		PresenceType presenceType = sender.getPresence();
		presence.setStatus(presenceType.getStatus());
		presence.setPriority(presenceType.getPriority());
		presence.setShow(presenceType.getShow());
		
		return presence; 
	}
	
}
