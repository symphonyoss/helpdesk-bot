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

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetOccupantListHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(GetOccupantListHandler.class);
	
	private IQ iqRequest;

	private Desk deskRoom;

	public GetOccupantListHandler(IQ iqRequest, Desk deskRoom)
	{
		this.iqRequest = iqRequest;
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", iqRequest.getTo().toBareJID());
		
		JID senderJID = iqRequest.getFrom();
		
		boolean isDeskMember = deskRoom.hasDeskMemberPrivilege(senderJID.toBareJID());
		
		List <User> occupantList = new ArrayList <User>();
		
		if (isDeskMember)
		{
			occupantList.addAll(deskRoom.getCurrentMembers());
			occupantList.addAll(deskRoom.getCurrentParticipants());
		}
		else
		{
			User virtualDeskUser = deskRoom.getVirtualDeskUser();
			occupantList.add(virtualDeskUser);
			
			User senderUser = deskRoom.getOccupantByJID(senderJID);
			
			if (senderUser != null)
			{
				occupantList.add(senderUser);
			}
		}
		
		packetList.add(IQResponse.createGetOccupantList(iqRequest, occupantList));
		
		Logger.info("{} has requested the occupant list of desk [{}]", iqRequest.getFrom(), deskRoom.getJID());
	}
}
