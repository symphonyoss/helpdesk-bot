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

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.collaboration.virtualdesk.handler.IActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.ResponseActionHandler;
import org.symphonyoss.collaboration.virtualdesk.handler.desk.RoomListHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.IPacketHandler;
import org.symphonyoss.collaboration.virtualdesk.packet.PacketUtils;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.handler.service.ServiceRegistrationHandler;

public class ServiceController implements IPacketHandler
{
	private static Logger Logger = LoggerFactory.getLogger(ServiceController.class);
	
	private IDeskDirectory deskDirectory;

	public ServiceController(IDeskDirectory deskDirectory)
	{
		this.deskDirectory = deskDirectory;
	}

	@Override
	public Collection <Packet> processIQ(IQ iq)
	{
		Logger.debug("ServiceController processing IQ Packet");
		
		String queryNamespace = PacketUtils.getIQNamespace(iq);

		IActionHandler actionHandler = new ResponseActionHandler(iq);

		if (queryNamespace.equalsIgnoreCase(Namespace.DISCO_INFO))
		{
			actionHandler = new ServiceRegistrationHandler(iq);
		}
		else if (queryNamespace.equalsIgnoreCase(Namespace.IQ_PING))
		{
			actionHandler = new ResponseActionHandler(iq);
		}
		else if (queryNamespace.equalsIgnoreCase(Namespace.DISCO_ITEMS))
		{
			actionHandler = new RoomListHandler(iq, deskDirectory);
		}

		return actionHandler.handle();
	}

	@Override
	public Collection <Packet> processPresence(Presence presence)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection <Packet> processMessage(Message message)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
