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

import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.IQ;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetDeskInfoHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(GetDeskInfoHandler.class);
	
	private IQ iqRequest;

	private Desk deskRoom;

	public GetDeskInfoHandler(IQ iqRequest, Desk deskRoom)
	{
		this.iqRequest = iqRequest;
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", iqRequest.getTo().toBareJID());
		
		String senderBareJID = iqRequest.getFrom().toBareJID();
		
		boolean isDeskMember = deskRoom.hasDeskMemberPrivilege(senderBareJID);
		
		if (isDeskMember)
		{
			int occupantCount = deskRoom.getCurrentMemberCount() + deskRoom.getCurrentParticipantCount();
			packetList.add(IQResponse.createDeskInfo(iqRequest, deskRoom.getName(), deskRoom.getDescription(), occupantCount));
			
			Logger.info("{} has requested desk info of desk [{}] [DeskMember: {}, OccupantCount: {}]",
					new Object[] {iqRequest.getFrom(), deskRoom.getJID(), isDeskMember, occupantCount});
		}
		else
		{
			int occupantCount = 1; // Count 1 for virtual desk user 
			
			occupantCount = deskRoom.getOccupantByJID(iqRequest.getFrom()) == null ? occupantCount : occupantCount + 1;
			
			packetList.add(IQResponse.createDeskInfo(iqRequest, deskRoom.getName(), deskRoom.getDescription(), occupantCount));
			
			Logger.info("{} has requested desk info of desk [{}] [DeskMember: {}, OccupantCount: 1]",
					new Object[] {iqRequest.getFrom(), deskRoom.getJID(), isDeskMember});
		}
	}
}
