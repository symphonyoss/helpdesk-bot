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

public class GetConferenceInfoHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(GetConferenceInfoHandler.class);
	
	private IQ iqRequest;

	private Desk conversationRoom;

	public GetConferenceInfoHandler(IQ iqRequest, Desk conversationRoom)
	{
		this.iqRequest = iqRequest;
		this.conversationRoom = conversationRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", iqRequest.getTo().toBareJID());
		
		packetList.add(IQResponse.createDeskInfo(iqRequest, conversationRoom.getName(), "", 2));
			
		Logger.info("{} has requested desk info of desk [{}] [DeskMember: {}, OccupantCount: {}]",
					new Object[] {iqRequest.getFrom(), conversationRoom.getJID(), 2});
			
	}
}
