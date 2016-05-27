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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.xmpp.packet.Message;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;

public class ViewMemberAvailableCommand extends AbstractCommand
{
	private static Logger Logger = LoggerFactory.getLogger(ViewMemberAvailableCommand.class);
	
	private String noMemberAvailable = "No member is available";
	private String memberAvailable = "There are %d in Desk.\nAvailable\n----------------------------------\n%s";
	
	public ViewMemberAvailableCommand()
	{
		commandText = "@members";
	}
	
	@Override
	protected boolean canProcess(String command, User senderUser, Desk deskRoomDesk)
	{
		if (senderUser.isDeskAdmin())
		{
			String[] commandWord = command.trim().split(" ");
			
			return commandWord[0].equalsIgnoreCase(commandText);	
		}

		return false;
	}

	@Override
	protected void internalProcess(String command, User senderUser, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		Logger.debug("User[{}] request to get member available of desk[{}]", senderUser.getJID(), deskRoom.getName());
		
		Message responseMessage = new Message();
		responseMessage.setFrom(deskRoom.getJID());
		responseMessage.setTo(senderUser.getJID());
		responseMessage.setType(Message.Type.groupchat);
		
		int userCount = 0;
		StringBuilder userNickname = new StringBuilder();
		
		for (User member : deskRoom.getCurrentMembers())
		{
			if (member.getPresence().equals(PresenceType.Online) || member.getPresence().equals(PresenceType.FreeToChat))
			{
				userCount++;
				userNickname.append(member.getNickname() + "\n");
			}
		}

		String message = (userCount > 0)? String.format(memberAvailable, userCount, userNickname) : noMemberAvailable;
		responseMessage.setBody(message);
		
		packetList.add(responseMessage);
		
		Logger.info("There are [{}] available in desk [{}]", userCount, deskRoom.getName());
	}

}
