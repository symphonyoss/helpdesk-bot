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

import java.util.Set;
import org.apache.log4j.MDC;
import org.dom4j.Element;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.PacketError;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestroyDeskHandler extends AbstractActionHandler
{
	private static Logger Logger = LoggerFactory.getLogger(DestroyDeskHandler.class);

	private IQ iqRequest;

	private Desk deskRoom;

	private IDeskDirectory deskDirectory;

	public DestroyDeskHandler(IQ iqRequest, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		this.iqRequest = iqRequest;
		this.deskRoom = deskRoom;
		this.deskDirectory = deskDirectory;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", deskRoom.getJID());
		
		Logger.debug("Starting destroy the Desk");

		Set <String> owners = deskRoom.getOwners();

		String senderBareJID = iqRequest.getFrom().toBareJID();

		// Allow only owner to destroy the desk
		if (owners.contains(senderBareJID))
		{
			Element queryElement = iqRequest.getChildElement();			
			Element destroyElement = queryElement.element("destroy");
			String reason = destroyElement.elementText("reason");
			
			//sent desk destroyed to user in the desk	
			packetList.addAll(PresenceResponse.destroyDeskResponse(deskRoom, reason));
			
			IQ destroyDeskResponse = IQ.createResultIQ(iqRequest);
			destroyDeskResponse.setType(Type.result);
			
			// destroyDesk response result IQ to actor
			packetList.add(destroyDeskResponse);
			
			Logger.info("{} has destroyed desk [{}].", senderBareJID, deskRoom.getJID());

			//destroy the desk
			deskDirectory.destroyDesk(deskRoom);
		}
		else
		{
			Logger.info("{} cannot destroy the desk because user is not desk owner of desk [{}].", iqRequest.getFrom(), deskRoom.getJID());
			packetList.add(IQResponse.createErrorResponse(iqRequest, PacketError.Condition.forbidden));
		}
	}

}
