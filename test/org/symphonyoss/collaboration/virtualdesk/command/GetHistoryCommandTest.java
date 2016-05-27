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

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearcher;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import org.symphonyoss.collaboration.virtualdesk.search.HistoryMessage;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearchResult;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class GetHistoryCommandTest
{
	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	
	private @Mocked @NonStrict
	HistorySearcher historySearcher;
	
	private @Mocked @NonStrict Desk deskRoom;
	private @Mocked @NonStrict IDeskDirectory deskDirectory;
	
	private @Mocked @NonStrict ICommand nextCommand;
	
	private GetHistoryCommand getHistoryCommand;
	
	private HistorySearchResult searchResult;
	
	private User senderUser;
	
	@Before
	public void before()
	{
		getHistoryCommand = new GetHistoryCommand(serviceConfiguration, historySearcher);
		getHistoryCommand.setNext(nextCommand);
		
		searchResult = new HistorySearchResult();
		
		senderUser = UserCreator.createMemberUser("user1");
	}
	
	@Test
	public void handle_DisableHistorySearch_NoProcessCommand()
	{
		new Expectations()
		{
			{
				serviceConfiguration.isHistorySearchEnabled(); result = false;
				
				nextCommand.process(anyString, (User)any, (Desk)any, (IDeskDirectory)any); times = 1;
			}
		};
		
		getHistoryCommand.process("@history", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_SyntaxIsIncorrect_ResponseHistorySyntaxHelpMessage()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				MessageResponse.createDeskMessageResponse("@history <#page>", (User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryCommand.process("@history 0 1", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_ThereIsNoHistoryMatched_ResponseNoHistoryMessage()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				searchResult.setTotalResult(0);
				
				MessageResponse.createDeskMessageResponse(
						"There is no history message.", (User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryCommand.process("@history 1", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestHistoryWithoutPageParam_RequestHistoryWithOffsetZero()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				searchResult.setTotalResult(0);
			}
		};
		
		getHistoryCommand.process("@history", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestHistoryWithPageLessThanOne_RequestHistoryWithOffsetZero()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				searchResult.setTotalResult(0);
			}
		};
		
		getHistoryCommand.process("@history 0", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestHistoryWithInvalidPageDataType_RequestHistoryWithOffsetZero()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				searchResult.setTotalResult(0);
			}
		};
		
		getHistoryCommand.process("@history abc", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestPageZeroAndThereAreHistoryMatchedFromOneDesk_ReturnPageOneOfHistory()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				Date now = new Date();
				
				searchResult.setTotalResult(2);
				searchResult.addHistoryMessage(new HistoryMessage("user2", "test2", "desk1", now));
				searchResult.addHistoryMessage(new HistoryMessage("user1", "test1", "desk1", now));
				
				MessageResponse.createDeskMessageResponse(
						"History page 1 of 1\r\n\r\ndesk1\r\n----------------------------" +
								String.format("\r\nuser1 \t%s \ttest1", DateUtils.formatMessageTimestamp(now)) +
								String.format("\r\nuser2 \t%s \ttest2", DateUtils.formatMessageTimestamp(now)),
								(User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryCommand.process("@history 1", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestPageZeroAndThereAreHistoryMatchedFromMultipleDesks_ReturnPageOneOfHistory()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				Date now = new Date();
				
				searchResult.setTotalResult(2);
				searchResult.addHistoryMessage(new HistoryMessage("user2", "test2", "desk2", now));
				searchResult.addHistoryMessage(new HistoryMessage("user1", "test1", "desk1", now));
				
				MessageResponse.createDeskMessageResponse(
						"History page 1 of 1\r\n\r\ndesk1\r\n----------------------------" +
							String.format("\r\nuser1 \t%s \ttest1", DateUtils.formatMessageTimestamp(now)) +
							"\r\n\r\ndesk2\r\n----------------------------" +
							String.format("\r\nuser2 \t%s \ttest2", DateUtils.formatMessageTimestamp(now)),
						(User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryCommand.process("@history 1", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_FailedToRequestHistoryFromElasticSearch_ResponseErrorMessage()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); result = new RuntimeException();
				
				MessageResponse.createDeskMessageResponse(
						"History is not currently available. Please try again later.",
								(User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryCommand.process("@history 1", senderUser, deskRoom, deskDirectory);
	}
}
