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
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * To handle all room listening
 * 
 * Test Idea
 * - No room exists
 * - Many room exists
 */
public class RoomListHandlerTest
{
	IDeskDirectory deskDirectory;
	RoomListHandler roomListHandler;
	
	@Before
	public void setUp() throws IOException {
		deskDirectory = mock(DeskDirectory.class);
	}
	
	@Test
	public void RoomList_NoRoomExists()
	{
		when(deskDirectory.getAllDesks()).thenReturn(new ArrayList<Desk>());
		
		RoomListHandler handler = new RoomListHandler(createIQRequest(), deskDirectory);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void RoomList_RoomExists()
	{
		Desk desk1 = new Desk("desk1", "localhost");
		List <Desk> rooms = new ArrayList<Desk>();
		rooms.add(desk1);
		
		when(deskDirectory.getAllDesks()).thenReturn(rooms);
		
		RoomListHandler handler = new RoomListHandler(createIQRequest(), deskDirectory);
		List<Packet> result = (List <Packet>) handler.handle();
		
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	//--Utility---------------------------
	private IQ createIQRequest(){
		IQ iq = new IQ();
		iq.setFrom(new JID("owner@virtualdesk.com"));
		iq.setTo(new JID("receiver@virtualdesk.com"));
		iq.setChildElement("x", Namespace.DISCO_INFO);
		
		return iq;
	}

}
