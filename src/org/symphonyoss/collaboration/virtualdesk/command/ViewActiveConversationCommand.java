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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.xmpp.packet.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewActiveConversationCommand extends AbstractCommand
{
	private static Logger Logger = LoggerFactory.getLogger(ViewActiveConversationCommand.class);
	
	private String conversationAvailable = "Total active chat session: %d";
	private String conversationPerMember = "\r\n%s = %d";
	
	public ViewActiveConversationCommand()
	{
		this.commandText = "@active";
	}
	
	@Override
	protected boolean canProcess(String command, User senderUser, Desk deskRoomDesk)
	{
		if (senderUser.isDeskAdmin())
		{
			String[] commandWord = command.trim().split(" ");
			
			return commandWord[0].equalsIgnoreCase(this.commandText);	
		}

		return false;
	}

	@Override
	protected void internalProcess(String command, User senderUser, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		Logger.debug("User[{}] request to get all conversation of desk [{}]", senderUser.getJID(), deskRoom.getName());
		
		Message responseMessage = new Message();
		responseMessage.setFrom(deskRoom.getJID());
		responseMessage.setTo(senderUser.getJID());
		responseMessage.setType(Message.Type.groupchat);
		
		int conversationCount = 0;

		StringBuilder message = new StringBuilder();
		Map<String, Integer> activeChatCountMap = new HashMap<String, Integer>();
		
		// Get all member in room
		for (User memberInRoom : deskRoom.getCurrentMembers())
		{
			activeChatCountMap.put(memberInRoom.getNickname().toLowerCase(), 0 );
		}
		
		Collection<Conversation> conversations = deskDirectory.getAllConversation();
		
		// Get all member with conversation
		if (conversations.size() > 0)
		{
			for (Conversation conversation : conversations)
			{
				if ( conversation.getParentDesk() == deskRoom )
				{
					Collection<User> members = conversation.getCurrentMembers();
					
					if (members.size() > 0)
					{
						conversationCount++;
						
						for (User member : members)
						{
//							String memberNickname = member.getNickname().toLowerCase();
							String memberNickname = deskRoom.getOccupantByJID(member.getJID()).getNickname().toLowerCase();
							
							Integer count = activeChatCountMap.get(memberNickname);
							
							if (count == null)
							{
								count = Integer.valueOf(0);
							}
							
							activeChatCountMap.put(memberNickname, Integer.valueOf(count.intValue() + 1));
						}
					}
				}
			}
		}
		
		message.append(String.format(conversationAvailable, conversationCount));
		
		if (conversationCount > 0)
		{
			message.append("\r\n---------------------------");
			
			for (Map.Entry<String, Integer> memberCount : activeChatCountMap.entrySet())
			{
				message.append(String.format(conversationPerMember, memberCount.getKey(), memberCount.getValue().intValue()));
			}
		}
		
		responseMessage.setBody(message.toString());
		
		packetList.add(responseMessage);
		
		Logger.info("There are [{}] available in conversation [{}]", conversationCount, deskRoom.getName());
	}
}
