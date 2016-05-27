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
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import org.symphonyoss.collaboration.virtualdesk.search.HistoryMessage;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearchResult;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetHistoryCommand extends AbstractCommand
{
	private static final Logger Logger = LoggerFactory.getLogger(GetHistoryCommand.class);
	
	protected IServiceConfiguration serviceConfiguration;
	protected HistorySearcher historySearcher;
	
	public GetHistoryCommand(IServiceConfiguration serviceConfiguration, HistorySearcher historySearcher)
	{
		this.serviceConfiguration = serviceConfiguration;
		this.historySearcher = historySearcher;
		
		commandText = "@history";
	}
	
	@Override
	protected boolean canProcess(String command, User senderUser, Desk deskRoom)
	{
		if (!serviceConfiguration.isHistorySearchEnabled())
		{
			return false;
		}
		
		String lowerCaseCommand = command.toLowerCase();
		
		return lowerCaseCommand.startsWith(commandText);
	}

	@Override
	protected void internalProcess(String command, User senderUser, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		Logger.debug("User[{}] request to retrieve history of desk [{}]", senderUser.getJID(), deskRoom.getName());
		
		String[] commandArgs = command.split(" ");
		
		if (commandArgs.length > 2)
		{
			packetList.add(MessageResponse.createDeskMessageResponse(
					"@history <#page>", senderUser, deskRoom.getJID()));
			
			return;
		}
		
		int offset = 0;
		
		if (commandArgs.length == 2)
		{
			offset = convertPageToOffset(commandArgs[1]);
		}
		
		retrieveHistory(senderUser, deskRoom, offset);
	}

	protected void retrieveHistory(User senderUser, Desk deskRoom, int offset)
	{
		Logger.info("Retrieving history of {} with offset {}", senderUser.getJID(), offset);
		
		try
		{
			HistorySearchResult result = historySearcher.search(senderUser.getJID().toBareJID(), deskRoom.getName(), offset, serviceConfiguration.getHistoryPageSize());
			
			long totalHits = result.getTotalResult();
			
			if (totalHits <= 0)
			{
				// No history returned from Elastic Search
				packetList.add(MessageResponse.createDeskMessageResponse(
						"There is no history message.", senderUser, deskRoom.getJID()));
				
				return;
			}
			
			long requestPage = (offset / serviceConfiguration.getHistoryPageSize()) + 1;
			long totalPage = (long)Math.ceil((double)result.getTotalResult() / serviceConfiguration.getHistoryPageSize());
			
			StringBuilder response = new StringBuilder();
			
			response.append("History page ");
			response.append(requestPage);
			response.append(" of ");
			response.append(totalPage);
			
			String deskHeader = null;
			
			for (HistoryMessage history : result.getHistoryMessages())
			{
				if (deskHeader == null || !deskHeader.equalsIgnoreCase(history.getDeskName()))
				{
					// Add new header because it starts new conversation
					deskHeader = history.getDeskName();
					
					response.append("\r\n\r\n");
					response.append(deskHeader);
					response.append("\r\n----------------------------");
				}
				
				response.append(String.format("\r\n%s \t%s \t%s", 
						history.getFromNickname(), 
						DateUtils.formatMessageTimestamp(history.getTimestamp()),
						history.getMessage()));
			}

			packetList.add(MessageResponse.createDeskMessageResponse(
					response.toString(), senderUser, deskRoom.getJID()));
		}
		catch (RuntimeException e)
		{
			Logger.warn("Failed to connect to Elastic Search to retrieve history", e);
			
			packetList.add(MessageResponse.createDeskMessageResponse(
					"History is not currently available. Please try again later.", senderUser, deskRoom.getJID()));
			

		}
	}
	
	protected int convertPageToOffset(String pageText)
	{
		int offset = 0;
		
		try
		{
			int pageNum = Integer.parseInt(pageText) - 1;
			
			if (pageNum < 0)
			{
				pageNum = 0;
			}
			
			offset = pageNum * serviceConfiguration.getHistoryPageSize();
		}
		catch (NumberFormatException e)
		{
			offset = 0;
		}
		
		return offset;
	}
}
