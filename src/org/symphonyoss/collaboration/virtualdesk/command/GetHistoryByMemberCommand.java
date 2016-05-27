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

package org.symphonyoss.collaboration.virtualdesk.command;

import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetHistoryByMemberCommand extends GetHistoryCommand
{
	private static final Logger Logger = LoggerFactory.getLogger(GetHistoryByMemberCommand.class);
	
	public GetHistoryByMemberCommand(IServiceConfiguration serviceConfiguration, HistorySearcher historySearcher)
	{
		super(serviceConfiguration, historySearcher);
	}

	@Override
	protected void internalProcess(String command, User senderUser, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		Logger.debug("User[{}] request to retrieve history of desk [{}]", senderUser.getJID(), deskRoom.getName());

		String[] commandArgs = command.split(" ");
		
		if (commandArgs.length > 3 || commandArgs.length < 2)
		{
			packetList.add(MessageResponse.createDeskMessageResponse(
					String.format("@history <nickname> <#page>"), senderUser, deskRoom.getJID()));
			
			return;
		}

		String nickname = commandArgs[1];
		
		User requestedUser = deskRoom.getOccupantByNickname(nickname);
		
		if (requestedUser == null)
		{
			// Cannot retrieve history for user that is not in desk because member does not know
			// JID of user. Virtual Desk is the only one to resolve to JID
			packetList.add(MessageResponse.createDeskMessageResponse(
					String.format("Cannot retrieve history for %s because user is not currently in desk.", nickname),
					senderUser,
					deskRoom.getJID()));
			
			return;
		}
		
		int offset = 0;
		
		if (commandArgs.length == 3)
		{
			offset = convertPageToOffset(commandArgs[2]);
		}

		retrieveHistory(senderUser, deskRoom, offset);
	}
}
