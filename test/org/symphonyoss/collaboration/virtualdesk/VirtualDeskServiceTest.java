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

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskDirectory;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;


public class VirtualDeskServiceTest
{
	private @Mocked @NonStrict IServiceConfiguration serviceInfo;
	private @Mocked @NonStrict DeskDirectory deskDirectory;

	private @Mocked @NonStrict ServiceController serviceController;
	private @Mocked @NonStrict DeskController deskController;
	private @Mocked @NonStrict DeskController conversationController;
	
	private VirtualDeskService virtualDeskService;
	
	private JID virtualDeskJID;
	private JID deskJID;
	private JID conversationJID;

	@Before
	public void before()
	{
		virtualDeskService = new VirtualDeskService(serviceInfo, deskDirectory, serviceController, deskController, conversationController);
		
		virtualDeskJID = new JID("virtualdesk.ucp.thomsonreutes.net");
		deskJID = JIDUtils.getNicknameJID("desk1", "abc");
		conversationJID = JIDUtils.getNicknameJID("desk1_23523", "");
	}
	
	@Test
	public void getDescription_AnyConditions_ReturnDescriptionFromServiceInfo()
	{
		new Expectations()
		{
			{
				serviceInfo.getServiceDescription(); times = 1;
			}
		};
		
		virtualDeskService.getDescription();
	}
	
	@Test
	public void getName_AnyConditions_ReturnNameFromServiceInfo()
	{
		new Expectations()
		{
			{
				serviceInfo.getVirtualDeskSubDomain(); times = 1;
			}
		};
		
		virtualDeskService.getName();
	}
	
	@Test
	public void initialize_InitializeFirstTime_CallSetDomainAndLoadDesks()
	{
		new Expectations()
		{
			{
				deskDirectory.setDomain(anyString); times = 1;

				deskDirectory.loadDesks(); times = 1;
			}
		};
		
		virtualDeskService.initialize(virtualDeskJID);
	}
	
	@Test
	public void initialize_InitializeSecondTime_NotCallSetDomainAndLoadDesksAgain()
	{
		new Expectations()
		{
			{
				deskDirectory.setDomain(anyString); times = 1;
				
				deskDirectory.loadDesks(); times = 1;
			}
		};
		
		virtualDeskService.initialize(virtualDeskJID);
		
		virtualDeskService.initialize(virtualDeskJID);
	}
	
	@Test
	public void processPacket_RecieveIQThatSentToService_CallServiceProcessPacket()
	{
		new Expectations()
		{
			{
				serviceController.processIQ((IQ)any); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setTo(virtualDeskJID);
		
		virtualDeskService.processPacket(iq);
	}
	
	@Test
	public void processPacket_RecievePresenceThatSentToService_CallServiceProcessPacket()
	{
		new Expectations()
		{
			{
				serviceController.processPresence((Presence)any); times = 1;
			}
		};
		
		Presence presence = new Presence();
		presence.setTo(virtualDeskJID);
		
		virtualDeskService.processPacket(presence);
	}
	
	@Test
	public void processPacket_RecieveMessageThatSentToService_CallServiceProcessPacket()
	{
		new Expectations()
		{
			{
				serviceController.processMessage((Message)any); times = 1;
			}
		};
		
		Message message = new Message();
		message.setTo(virtualDeskJID);
		
		virtualDeskService.processPacket(message);
	}
	
	@Test
	public void processPacket_RecieveIQThatSentToDesk_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskController.processIQ((IQ)any); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setTo(deskJID);
		
		virtualDeskService.processPacket(iq);
	}
	
	@Test
	public void processPacket_RecievePresenceThatSentToDesk_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskController.processPresence((Presence)any); times = 1;
			}
		};
		
		Presence presence = new Presence();
		presence.setTo(deskJID);
		
		virtualDeskService.processPacket(presence);
	}
	
	@Test
	public void processPacket_RecieveMessageThatSentToDesk_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskController.processMessage((Message)any); times = 1;
			}
		};
		
		Message message = new Message();
		message.setTo(deskJID);
		
		virtualDeskService.processPacket(message);
	}
	
	@Test
	public void processPacket_RecieveIQThatSentToDeskConversation_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskDirectory.containsConversation(anyString); result = true;
				
				conversationController.processIQ((IQ)any); times = 1;
			}
		};
		
		IQ iq = new IQ();
		iq.setTo(conversationJID);
		
		virtualDeskService.processPacket(iq);
	}
	
	@Test
	public void processPacket_RecievePresenceThatSentToDeskConversation_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskDirectory.containsConversation(anyString); result = true;

				conversationController.processPresence((Presence)any); times = 1;
			}
		};
		
		Presence presence = new Presence();
		presence.setTo(conversationJID);
		
		virtualDeskService.processPacket(presence);
	}
	
	@Test
	public void processPacket_RecieveMessageThatSentToDeskConversation_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskDirectory.containsConversation(anyString); result = true;

				conversationController.processMessage((Message)any); times = 1;
			}
		};
		
		Message message = new Message();
		message.setTo(conversationJID);
		
		virtualDeskService.processPacket(message);
	}
	
	@Test
	public void processPacket_RecievePacketThatIsNotIQOrPresenceOrMessage_CallDeskProcessPacket()
	{
		new Expectations()
		{
			{
				deskController.processIQ((IQ)any); times = 0;
				deskController.processPresence((Presence)any); times = 0;
				deskController.processMessage((Message)any); times = 0;
			}
		};
		
		Packet packet = new Packet()
		{
			private JID toJID;
			
			@Override
			public JID getTo()
			{
				return toJID;
			}
			
			@Override
			public void setTo(JID jid)
			{
				toJID = jid;
			}
			
			@Override
			public Packet createCopy()
			{
				return null;
			}
		};
		
		packet.setTo(deskJID);
		
		virtualDeskService.processPacket(packet);
	}
	
	@Test
	public void processPacket_ExceptionIsThrownFromController_DoNothingAndExceptionIsNotThrown()
	{
		new Expectations()
		{

		};
		
		Message message = new Message();
		message.setTo(virtualDeskJID);
		
		virtualDeskService.processPacket(message);
	}
}
