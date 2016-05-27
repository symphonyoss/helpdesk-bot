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

package org.symphonyoss.collaboration.virtualdesk.packet;

import java.util.List;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

public class PresenceResponseTest
{
	@Before
	public void before()
	{
		new PresenceResponse();
	}
	
	@Test
	public void createPresenceError_WithUserAndDeskJIDParams_ReturnPresenceError()
	{
		User user = new User(JIDUtils.getUserJID("user1"), "user1_nickname");
		
		JID deskJID = JIDUtils.getNicknameJID("desk1", null);
		
		Presence error = (Presence)PresenceResponse.createPresenceError(user, deskJID, PacketError.Condition.conflict);
		
		Assert.assertEquals(JIDUtils.getNicknameJID(deskJID.getNode(), user.getNickname()), error.getFrom());
		Assert.assertEquals(user.getJID(), error.getTo());
		Assert.assertEquals(Presence.Type.error, error.getType());
		Assert.assertEquals(PacketError.Condition.conflict, error.getError().getCondition());
	}

	@Test
	public void createPresenceError_WithPresenceParam_ReturnPresenceError()
	{
		User user = new User(JIDUtils.getUserJID("user1"), "user1_nickname");
		
		JID deskJID = JIDUtils.getNicknameJID("desk1", user.getNickname());
		
		Presence presence = new Presence();
		presence.setFrom(user.getJID());
		presence.setTo(deskJID);
		
		Presence error = (Presence)PresenceResponse.createPresenceError(presence, PacketError.Condition.internal_server_error);
		
		Assert.assertEquals(deskJID, error.getFrom());
		Assert.assertEquals(user.getJID(), error.getTo());
		Assert.assertEquals(Presence.Type.error, error.getType());
		Assert.assertEquals(PacketError.Condition.internal_server_error, error.getError().getCondition());
	}

	@Test
	public void createPresenceError_WithPresenceAndMessageParams_ReturnPresenceError()
	{
		User user = new User(JIDUtils.getUserJID("user1"), "user1_nickname");
		
		JID deskJID = JIDUtils.getNicknameJID("desk1", user.getNickname());
		
		Presence presence = new Presence();
		presence.setFrom(user.getJID());
		presence.setTo(deskJID);
		
		String errorText = "This is error text.";
		
		Presence error = (Presence)PresenceResponse.createPresenceError(presence, PacketError.Condition.not_allowed, errorText);
		
		Assert.assertEquals(deskJID, error.getFrom());
		Assert.assertEquals(user.getJID(), error.getTo());
		Assert.assertEquals(Presence.Type.error, error.getType());
		Assert.assertEquals(PacketError.Condition.not_allowed, error.getError().getCondition());
		Assert.assertEquals(errorText, error.getError().getText());
	}
	
	@Test
	public void destroyPresence_UserActiveWithReason()
	{
		User owner1 = UserCreator.createOwnerUser("owner.User1");
		User participnt1 = UserCreator.createParticipantUser("particapant1");
		Desk desk1 = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		desk1.addOccupant(owner1);
		desk1.addOccupant(participnt1);
		String message = "Force detroy the room";
		
		List<Packet> packetList = (List <Packet>) PresenceResponse.destroyDeskResponse(desk1, message);
		
		Assert.assertEquals(2, packetList.size());
		for(Packet presence : packetList)
		{
			presence = (Presence)presence;
			Assert.assertEquals(Presence.Type.unavailable, ((Presence)presence).getType());	
			Assert.assertEquals(desk1.getJID().toBareJID(), presence.getFrom().toBareJID());
			Element item = ((Presence)presence).getChildElement("x", Namespace.MUC_USER).element("item");
			Assert.assertEquals(Affiliation.none.toString(), item.attributeValue("affiliation"));
			Assert.assertEquals(Affiliation.none.toString(), item.attributeValue("role"));
			Element destroy  = ((Presence)presence).getChildElement("x", Namespace.MUC_USER).element("destroy");
			Assert.assertEquals(message, destroy.elementText("reason"));
		}
	}
	
	@Test
	public void destroyPresence_UserActiveWithNullReason()
	{
		User owner1 = UserCreator.createOwnerUser("owner.User1");
		User participnt1 = UserCreator.createParticipantUser("particapant1");
		Desk desk1 = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		desk1.addOccupant(owner1);
		desk1.addOccupant(participnt1);
		String message = null;
		
		List<Packet> packetList = (List <Packet>) PresenceResponse.destroyDeskResponse(desk1, message);
		System.out.println(packetList);
		Assert.assertEquals(2, packetList.size());
		for(Packet presence : packetList)
		{
			presence = (Presence)presence;
			Assert.assertEquals(Presence.Type.unavailable, ((Presence)presence).getType());	
			Assert.assertEquals(desk1.getJID().toBareJID(), presence.getFrom().toBareJID());
			Element item = ((Presence)presence).getChildElement("x", Namespace.MUC_USER).element("item");
			Assert.assertEquals(Affiliation.none.toString(), item.attributeValue("affiliation"));
			Assert.assertEquals(Affiliation.none.toString(), item.attributeValue("role"));
			Element destroy  = ((Presence)presence).getChildElement("x", Namespace.MUC_USER).element("destroy");
			Assert.assertEquals(StringUtils.EMPTY, destroy.elementText("reason"));
		}
	}
	
	@Test
	public void destroyPresence_UserActiveWithEmptyReason()
	{
		User owner1 = UserCreator.createOwnerUser("owner.User1");
		User participnt1 = UserCreator.createParticipantUser("particapant1");
		Desk desk1 = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		desk1.addOccupant(owner1);
		desk1.addOccupant(participnt1);
		String message = "";
		
		List<Packet> packetList = (List <Packet>) PresenceResponse.destroyDeskResponse(desk1, message);
		System.out.println(packetList);
		Assert.assertEquals(2, packetList.size());
		for(Packet presence : packetList)
		{
			presence = (Presence)presence;
			Assert.assertEquals(Presence.Type.unavailable, ((Presence)presence).getType());	
			Assert.assertEquals(desk1.getJID().toBareJID(), presence.getFrom().toBareJID());
			Element item = ((Presence)presence).getChildElement("x", Namespace.MUC_USER).element("item");
			Assert.assertEquals(Affiliation.none.toString(), item.attributeValue("affiliation"));
			Assert.assertEquals(Affiliation.none.toString(), item.attributeValue("role"));
			Element destroy  = ((Presence)presence).getChildElement("x", Namespace.MUC_USER).element("destroy");
			Assert.assertEquals(message, destroy.elementText("reason"));
		}
	}
}
