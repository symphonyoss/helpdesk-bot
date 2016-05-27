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

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.component.ComponentException;
import org.xmpp.component.RemoteNode;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.component.IRemoteComponent;
import org.symphonyoss.collaboration.virtualdesk.component.OpenfirePlusRemoteComponent;
import org.symphonyoss.collaboration.virtualdesk.component.RemoteComponentException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class VirtualDeskServiceProxyTest
{
	private VirtualDeskServiceProxy serviceProxy;
	
	private @Mocked @NonStrict VirtualDeskService virtualDeskService;

	
	@Before
	public void before()
	{
		serviceProxy = new VirtualDeskServiceProxy("Server1", virtualDeskService);
	}
	
	@Test
	public void getDescription_AnyConditions_ReturnDescriptionFromVirtualDeskService()
	{
		new Expectations()
		{
			{
				virtualDeskService.getDescription(); times = 1;
			}
		};
		
		serviceProxy.getDescription();
	}
	
	@Test
	public void getName_AnyConditions_ReturnNameFromVirtualDeskService()
	{
		new Expectations()
		{
			{
				virtualDeskService.getName(); times = 1;
			}
		};
		
		serviceProxy.getName();
	}
	
	@Test
	public void initialize_AnyConditions_CallVirtualDeskServiceInitilize() throws ComponentException
	{
		new Expectations()
		{
			{
				virtualDeskService.initialize((JID)any); times = 1;
			}
		};
		
		serviceProxy.initialize(null,  null);
	}
	
	@Test
	public void processPacket_HasIncomingPacket_IncrementCounter() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				virtualDeskService.processPacket((Packet)any); result = new ArrayList<Packet>();
				

			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void processPacket_NoPacketReturn_DoNotSendPacketToOpenfire() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				virtualDeskService.processPacket((Packet)any); result = new ArrayList<Packet>();
				
				setField(serviceProxy, "remoteComponent", remoteComponent);
				
				remoteComponent.sendPacket((Packet)any); times = 0;
			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void processPacket_HavePacketReturn_SendPacketToOpenfire() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				List<Packet> packetList = new ArrayList<Packet>();
				packetList.add(new Message());
				packetList.add(new Message());
						
				virtualDeskService.processPacket((Packet)any); result = packetList;
				
				setField(serviceProxy, "remoteComponent", remoteComponent);
				
				remoteComponent.sendPacket((Packet)any); times = packetList.size();
			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void processPacket_ConnectionLostWhenSendMessageBackToOpenfire_IncrementCounterError() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				List<Packet> packetList = new ArrayList<Packet>();
				packetList.add(new Message());
				
				virtualDeskService.processPacket((Packet)any); result = packetList;
				
				setField(serviceProxy, "remoteComponent", remoteComponent);
				
				remoteComponent.sendPacket((Packet)any); 
				result = new RemoteComponentException("Error", null);
				times = packetList.size();
				

			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void processPacket_ConnectionLostWhenSendMessageBackToOpenfire_IgnoreAndContinueSendingNextMessage() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				List<Packet> packetList = new ArrayList<Packet>();
				packetList.add(new Message());
				packetList.add(new Message());
				
				virtualDeskService.processPacket((Packet)any); result = packetList;
				
				setField(serviceProxy, "remoteComponent", remoteComponent);
				
				remoteComponent.sendPacket((Packet)any); 
				result = new RemoteComponentException("Error", null);
				times = packetList.size();
			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void processPacket_UnexpectedExceptionOccuredWhenSendMessageBackToOpenfire_IncrementCounterError() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				List<Packet> packetList = new ArrayList<Packet>();
				packetList.add(new Message());
				
				virtualDeskService.processPacket((Packet)any); result = packetList;
				
				setField(serviceProxy, "remoteComponent", remoteComponent);
				
				remoteComponent.sendPacket((Packet)any); 
				result = new Exception();
				times = packetList.size();
				

			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void processPacket_UnexpectedExceptionOccuredWhenSendMessageBackToOpenfire_IgnoreAndContinueSendingNextMessage() throws RemoteComponentException
	{
		new Expectations()
		{
			@Mocked @NonStrict IRemoteComponent remoteComponent;
			{
				List<Packet> packetList = new ArrayList<Packet>();
				packetList.add(new Message());
				packetList.add(new Message());
				
				virtualDeskService.processPacket((Packet)any); result = packetList;
				
				setField(serviceProxy, "remoteComponent", remoteComponent);
				
				remoteComponent.sendPacket((Packet)any); 
				result = new Exception();
				times = packetList.size();
			}
		};
		
		serviceProxy.processPacket(null);
	}
	
	@Test
	public void setRemoteNode_AnyConditions_CreateOpenfirePlusRemoteComponentInstance()
	{
		new Expectations()
		{
			@Mocked OpenfirePlusRemoteComponent remoteComponent;
			{
				new OpenfirePlusRemoteComponent((RemoteNode)any);
			}
		};
		
		serviceProxy.setRemoteNode(null, "Server1");
	}
	
	@Test
	public void shutdown_AnyConditions_LogMessage()
	{
		serviceProxy.shutdown();
	}
	
	@Test
	public void start_AnyConditions_LogMessage()
	{
		serviceProxy.start();
	}
	
	@Test
	public void connectionLost_AnyConditions_LogMessage()
	{
		serviceProxy.connectionLost();
	}
}
