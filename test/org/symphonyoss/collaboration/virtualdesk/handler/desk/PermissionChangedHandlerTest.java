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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;


public class PermissionChangedHandlerTest
{
	private Desk deskRoom;
	private User roomowner;
	
	@Before
	public void setUp() throws IOException {
		
		roomowner = UserCreator.createOwnerUser("Owner.User01");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		deskRoom.addOwner(roomowner.getJID().toBareJID());
		deskRoom.addOccupant(roomowner);
	}
	
	@Test
	public void memberOnly_UserInMemberList()
	{
		User participant01 = UserCreator.createParticipantUser("Participant.User01");
		
		deskRoom.addOccupant(participant01);
		deskRoom.addParticipants(participant01.getJID().toBareJID());
		deskRoom.isMembersOnly(true);
		
		List <Packet> result = (List <Packet>) new PermissionChangedHandler(deskRoom).handle();

		assertEquals(participant01, deskRoom.getOccupantByJID(participant01.getJID()));
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void memberOnly_UserNotInMemberList()
	{
		User participant01 = UserCreator.createParticipantUser("Participant.User01");
		
		deskRoom.addOccupant(participant01);
		deskRoom.isMembersOnly(true);
		
		List <Packet> result = (List <Packet>) new PermissionChangedHandler(deskRoom).handle();

		assertNull(deskRoom.getOccupantByJID(participant01.getJID()));
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(participant01.getJID(), result.get(0).getTo());
		assertEquals(Presence.Type.unavailable, ((Presence)result.get(0)).getType());
		assertEquals(roomowner.getJID(), result.get(1).getTo());
		assertEquals(Presence.Type.unavailable, ((Presence)result.get(1)).getType());
	}
	
	@Test
	public void memberOnly_AdminToParticipant_Member()
	{
		User admin01 = UserCreator.createAdminUser("admin.user01");
		
		deskRoom.addOccupant(admin01);
		deskRoom.isMembersOnly(true);
		deskRoom.addParticipants(admin01.getJID().toBareJID());
		
		List <Packet> result = (List <Packet>) new PermissionChangedHandler(deskRoom).handle();

		assertEquals(admin01, deskRoom.getOccupantByJID(admin01.getJID()));
		assertEquals(1, deskRoom.getCurrentMembers().size());
		assertNotNull(result);
		assertEquals(5, result.size());
	}
	
	@Test
	public void memberOnly_AdminToParticipant_NoMember()
	{
		User admin01 = UserCreator.createAdminUser("admin.user01");
		
		deskRoom.addOccupant(admin01);
		deskRoom.isMembersOnly(true);
		
		List <Packet> result = (List <Packet>) new PermissionChangedHandler(deskRoom).handle();
		System.out.println(result);
		assertNull(deskRoom.getOccupantByJID(admin01.getJID()));
		assertEquals(1, deskRoom.getCurrentMembers().size());
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(admin01.getJID(), result.get(0).getTo());
		assertEquals(Presence.Type.unavailable, ((Presence)result.get(0)).getType());
		assertEquals(roomowner.getJID(), result.get(1).getTo());
		assertEquals(Presence.Type.unavailable, ((Presence)result.get(1)).getType());
	}
	
	@Test
	public void handle_ChangeAffiliationFromOwnerToMember_()
	{
		User owner = UserCreator.createOwnerUser("owner.user");
		User willBemember = UserCreator.createOwnerUser("member.user");
		
		deskRoom.addOwner(owner.getJID().toBareJID());
		deskRoom.addMember(willBemember.getJID().toBareJID());
		deskRoom.addOccupant(owner);
		deskRoom.addOccupant(willBemember);
		
		PermissionChangedHandler handler = new PermissionChangedHandler(deskRoom);
		
		handler.handle();
		
		Assert.assertEquals(Affiliation.member, willBemember.getAffiliation());
	}
}
