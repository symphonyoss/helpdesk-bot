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

package org.symphonyoss.collaboration.virtualdesk;

import java.io.IOException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.RoomListHandler;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.handler.ResponseActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.service.ServiceRegistrationHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;

public class ServiceControllerTest
{

	private @Mocked @NonStrict DeskDirectory deskDirectory;
	
	private ServiceController serviceController;
	
	@Before
	public void setUp() throws IOException {
	
		serviceController = new ServiceController(deskDirectory);
	}
	
	@Test
	public void processIQ_ServiceRegistrationHandler(){
		
		new Expectations()
		{
			@Mocked @NonStrict ServiceRegistrationHandler handler;
			{
				handler.handle(); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setChildElement("query", Namespace.DISCO_INFO);
		serviceController.processIQ(iq);
	}
	
	@Test
	public void processIQ_ResponseActionHandler(){
		
		new Expectations()
		{
			@Mocked @NonStrict ResponseActionHandler handler;
			{
				handler.handle(); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setChildElement("query", Namespace.IQ_PING);
		serviceController.processIQ(iq);
	}
	
	@Test
	public void processIQ_RoomListHandler(){
		
		new Expectations()
		{
			@Mocked @NonStrict
			RoomListHandler handler;
			{
				handler.handle(); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setChildElement("query", Namespace.DISCO_ITEMS);
		serviceController.processIQ(iq);
	}
	
	@Test
	public void processIQ_UnHandleIQ(){
		
		new Expectations()
		{
			@Mocked @NonStrict ResponseActionHandler handler;
			{
				handler.handle(); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setChildElement("query", Namespace.IQ_VERSION);
		serviceController.processIQ(iq);
	}
	
	@Test
	public void processPresence(){
		
		Presence presence = new Presence();
		serviceController.processPresence(presence);
	}
	
	@Test
	public void processMessage(){
		
		Message message = new Message();
		serviceController.processMessage(message);
	}
}
