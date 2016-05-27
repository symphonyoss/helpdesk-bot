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
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetDeskFormHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(GetDeskFormHandler.class);
	
	private IQ iqRequest;

	private Desk deskRoom;

	public GetDeskFormHandler(IQ iqRequest, Desk deskRoom)
	{
		this.iqRequest = iqRequest;
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", iqRequest.getTo().toBareJID());
		
		Logger.debug("Start getting desk properties form");
		
		Set <String> owners = deskRoom.getOwners();

		String senderBareJID = iqRequest.getFrom().toBareJID();

		if (owners.contains(senderBareJID))
		{
			packetList.add(IQResponse.createGetDeskForm(iqRequest, deskRoom));
			
			Logger.info("{} has requested desk form of desk [{}].", iqRequest.getFrom(), deskRoom.getJID());
		}
		else
		{
			Logger.debug(String.format("Permission denied for user: %s", senderBareJID));
			packetList.add(IQResponse.createErrorResponse(iqRequest, PacketError.Condition.not_authorized));
			
			Logger.info("{} cannot request desk form because user is not desk owner of desk [{}].", 
					iqRequest.getFrom(), deskRoom.getJID());
		}
		
		Logger.debug("Finished getting desk properties form");
	}
}
