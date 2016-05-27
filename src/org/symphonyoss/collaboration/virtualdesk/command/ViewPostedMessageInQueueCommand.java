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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.xmpp.packet.Message;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;

public class ViewPostedMessageInQueueCommand extends AbstractCommand
{
	private static Logger Logger = LoggerFactory.getLogger(ViewPostedMessageInQueueCommand.class);
	
	private String noMessageInQueue = "No posted message in queue";
	private String messageInQueue = "There are %d posted message in queue.\n----------------------------------\n%s";
	
	public ViewPostedMessageInQueueCommand()
	{
		commandText = "@queue";
	}
	
	@Override
	protected boolean canProcess(String command, User senderUser, Desk deskRoomDesk)
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
		Logger.debug("User[{}] request to get posted message in queue of desk[{}]", senderUser.getJID(), deskRoom.getName());
		
		Message responseMessage = new Message();
		responseMessage.setFrom(deskRoom.getJID());
		responseMessage.setTo(senderUser.getJID());
		responseMessage.setType(Message.Type.groupchat);
		
		int messageCount = 0;
		StringBuilder queue = new StringBuilder();
		
		List<UserState> userStateList = new ArrayList<UserState>(deskRoom.getAllQuestions());
		
		for (UserState state : userStateList)
		{
			if (state.getState().equals(WorkflowState.AwaitResponse))
			{
				messageCount++;
				queue.append(String.format("%s \t%s \t%s", state.getPosterNickname(), DateUtils.formatMessageTimestamp(state.getTimestamp()), formatMessage(state.getQuestions())));
			}
		}

		String message = (messageCount > 0)? String.format(messageInQueue, messageCount, queue) : noMessageInQueue;
		responseMessage.setBody(message);
		
		packetList.add(responseMessage);
		
		Logger.info("There are [{}] posted message in queue in desk [{}]", messageCount, deskRoom.getName());
	}
	
	private String formatMessage(List<String> questions)
	{
		StringBuilder message = new StringBuilder();
		
		for(String question : questions)
		{
			message.append(String.format("%s\n", question));
		}
		
		return message.toString();
	}

}
