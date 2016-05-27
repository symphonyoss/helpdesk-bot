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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DestroyDeskHandlerTest
{
	private Desk deskRoom;
	private User roomowner;
	private DestroyDeskHandler destroyHandler;
	
	private @Mocked @NonStrict
	DeskDirectory deskDirectory;


	
	private IQ iq;
	
	@Before
	public void setUp() throws IOException {
	
		roomowner = UserCreator.createOwnerUser("Owner.User01");

		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
		deskRoom.addOwner(roomowner.getJID().toBareJID());
		deskRoom.addOccupant(roomowner);
		
		iq = creatIQRequest();
	}
	
	@Test
	public void detroyDesk_NoPermission()
	{
		destroyHandler = new DestroyDeskHandler(iq, deskRoom, deskDirectory);
		
		final User sender = UserCreator.createOwnerUser("sender");
		iq.setFrom(sender.getJID());
		
		List<Packet> result = (List <Packet>) destroyHandler.handle();
		
		IQ packet = (IQ)result.get(0);
	
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(IQ.Type.error, packet.getType());
		assertEquals(PacketError.Condition.forbidden, packet.getError().getCondition());
	}

	@Test
	public void detroyDesk_OnlyOneOwnerInRoom()
	{	
		destroyHandler = new DestroyDeskHandler(iq, deskRoom, deskDirectory);
		
		final List<Packet> returnPacket = new ArrayList<Packet>();
		final Presence presence = new Presence(Presence.Type.unavailable);
		presence.setFrom(deskRoom.getJID());
		presence.setTo(roomowner.getJID());
		returnPacket.add(presence);
		
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				PresenceResponse.destroyDeskResponse((Desk)any, (String)any); times=1; result=returnPacket;
			}
		};
		
		List<Packet> result = (List <Packet>) destroyHandler.handle();
		System.out.println(result);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(Presence.Type.unavailable, ((Presence) result.get(0)).getType());
		assertEquals(IQ.Type.result, ((IQ) result.get(1)).getType());
	}
	
	@Test
	public void detroyDesk_WithNoReason()
	{	
		iq = new IQ(Type.set);
		iq.setFrom(roomowner.getJID());
		iq.setTo(deskRoom.getJID());
		iq.setChildElement("query", Namespace.MUC_OWNER).addElement("destroy").addAttribute("jid", deskRoom.getJID().toBareJID());
		
		destroyHandler = new DestroyDeskHandler(iq, deskRoom, deskDirectory);
		
		final List<Packet> returnPacket = new ArrayList<Packet>();
		final Presence presence = new Presence(Presence.Type.unavailable);
		presence.setFrom(deskRoom.getJID());
		presence.setTo(roomowner.getJID());
		returnPacket.add(presence);
		
		new Expectations()
		{
			@Mocked @NonStrict PresenceResponse presenceResponse;
			{
				PresenceResponse.destroyDeskResponse((Desk)any, (String)any); times=1; result=returnPacket;
			}
		};
		
		List<Packet> result = (List <Packet>) destroyHandler.handle();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(Presence.Type.unavailable, ((Presence) result.get(0)).getType());
		assertEquals(IQ.Type.result, ((IQ) result.get(1)).getType());
	}
	
	//**** Utils
	private IQ creatIQRequest(){
		IQ iq = new IQ(Type.set);
		iq.setFrom(roomowner.getJID());
		iq.setTo(deskRoom.getJID());
		
		Element qElement = iq.setChildElement("query", Namespace.MUC_OWNER);
		
		Element destroyElement = qElement.addElement("destroy").addAttribute("jid", deskRoom.getJID().toBareJID());
		
		destroyElement.addElement("reason").setText("Unit Tset Reason");
	
		return iq;
	}
	
}
