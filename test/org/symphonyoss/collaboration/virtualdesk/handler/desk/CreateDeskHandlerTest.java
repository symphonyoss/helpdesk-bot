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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.Assert;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

public class CreateDeskHandlerTest
{
	private CreateDeskHandler handler;

	private Presence mockPresence;
	private IDeskDirectory mockDeskDirectory;
	
	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	


	@Before
	public void before()
	{
		mockPresence = mock(Presence.class);

		mockDeskDirectory = mock(IDeskDirectory.class);

		handler = new CreateDeskHandler(mockPresence, mockDeskDirectory, serviceConfiguration);
	}

	@Test
	public void handle_NumberOfCreatedDeskIsMoreThanMaxLimit_ReturnPresenceError()
	{
		when(mockPresence.getFrom()).thenReturn(new JID("User1", TestConst.VIRTUALDESK_DOMAIN, null));
		when(mockPresence.getTo()).thenReturn(new JID("Desk1", TestConst.VIRTUALDESK_DOMAIN, null));
		when(mockDeskDirectory.getCurrentNumberOfDeskCreated(anyString())).thenReturn(2);

		List <Packet> packetList = (List <Packet>) handler.handle();

		Assert.assertEquals(1, packetList.size());
		Assert.assertEquals(Presence.Type.error, ((Presence) packetList.get(0)).getType());
	}

	@Test
	public void handle_NumberOfCreatedDeskIsLessThanMaxLimit_InitDesk()
	{
		when(mockPresence.getFrom()).thenReturn(new JID("User1", TestConst.VIRTUALDESK_DOMAIN, null));
		when(mockPresence.getTo()).thenReturn(new JID("Desk1", TestConst.VIRTUALDESK_DOMAIN, null));
		when(mockDeskDirectory.getCurrentNumberOfDeskCreated(anyString())).thenReturn(1);

		Desk desk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);

		when(mockDeskDirectory.createDesk(eq("desk1"), anyString())).thenReturn(desk);

		handler.handle();

		Assert.assertTrue(desk.isNewlyCreated());
	}
}
