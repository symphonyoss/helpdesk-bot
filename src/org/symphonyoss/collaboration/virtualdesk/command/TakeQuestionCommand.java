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

import org.apache.commons.lang.RandomStringUtils;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.muc.Conversation;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;

public class TakeQuestionCommand extends AbstractCommand
{
	public TakeQuestionCommand()
	{
		commandText = "@take";
	}

	@Override
	protected boolean canProcess(String command, User senderUser, Desk deskRoom)
	{
		if (senderUser.isDeskMember())
		{
			String[] commandWord = command.trim().split(" ");
			
			return commandWord[0].equalsIgnoreCase(commandText);	
		}

		return false;
	}

	@Override
	protected void internalProcess(String command, User senderUser, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		int spaceIndex = command.indexOf(" ");

		if (spaceIndex < 0)
		{
			packetList.add(MessageResponse.createDeskMessageResponse("@take <nickname>", senderUser, deskRoom.getJID()));
		}
		else
		{
			String posterNickname = command.substring(spaceIndex + 1).trim().toLowerCase();

			UserState userState = deskRoom.getQuestion(posterNickname);

			if (userState == null)
			{
				String notFoundQuestionMessage = String.format("No question of %s is posted in Desk.", posterNickname);
				packetList.add(MessageResponse.createDeskMessageResponse(notFoundQuestionMessage, senderUser, deskRoom.getJID()));
			}
			else if (userState.getState() != WorkflowState.AwaitResponse)
			{
				String questionAlreadyTakenMessage = String.format("Question of %s has already been taken.", posterNickname);
				packetList.add(MessageResponse.createDeskMessageResponse(questionAlreadyTakenMessage, senderUser, deskRoom.getJID()));
			}
			else
			{
				userState.setState(WorkflowState.InConversation);
				
				String pickupMessageBody = String.format("%s has taken the question of %s", senderUser.getNickname(), posterNickname);
				packetList.addAll(MessageResponse.createDeskMessageResponse(pickupMessageBody, deskRoom.getCurrentMembers(), deskRoom.getJID()));
				
				// Invite Action
				String newDeskRoomName = deskRoom.getName() + "_" + RandomStringUtils.randomNumeric(5);
				String newDeskRoomJID =  newDeskRoomName + "@" + deskRoom.getJID().getDomain();
				
				// Message to participant
				packetList.add(MessageResponse.createJoinConversationInvite(senderUser, newDeskRoomJID, deskRoom));
				
				// Message to member
				packetList.add(MessageResponse.createJoinConversationInvite(userState.getPosterJID(), newDeskRoomJID, deskRoom));
				
				// Add Conference to Desk directory
				Conversation conversation = deskDirectory.createDeskConversation(newDeskRoomName, newDeskRoomJID, deskRoom);
				
				// Configure Conference
				conversation.setQuestion(userState);
				conversation.setDeskAliasName(deskRoom.getDeskAliasName());
				
				conversation.addOwner(senderUser.getJID().toBareJID());
				
				conversation.setDeskDirectory(deskDirectory);
			}
		}
	}
}
