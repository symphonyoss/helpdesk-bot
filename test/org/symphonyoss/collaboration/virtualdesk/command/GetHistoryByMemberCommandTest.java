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

import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearchResult;
import org.symphonyoss.collaboration.virtualdesk.search.HistorySearcher;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.JID;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class GetHistoryByMemberCommandTest
{
	private @Mocked @NonStrict
	IServiceConfiguration serviceConfiguration;
	
	private @Mocked @NonStrict
	HistorySearcher historySearcher;
	
	private @Mocked @NonStrict
	Desk deskRoom;
	private @Mocked @NonStrict
	IDeskDirectory deskDirectory;
	
	private @Mocked @NonStrict ICommand nextCommand;
	
	private GetHistoryByMemberCommand getHistoryByMemberCommand;
	
	private HistorySearchResult searchResult;
	
	private User senderUser;
	
	@Before
	public void before()
	{
		getHistoryByMemberCommand = new GetHistoryByMemberCommand(serviceConfiguration, historySearcher);
		getHistoryByMemberCommand.setNext(nextCommand);
		
		searchResult = new HistorySearchResult();
		
		senderUser = UserCreator.createMemberUser("user1");
	}

	@Test
	public void handle_RequestHistoryWithNoParam_ResponseHelpMessage()
	{
		new Expectations()
		{
			@Mocked @NonStrict
			MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				MessageResponse.createDeskMessageResponse("@history <nickname> <#page>", (User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryByMemberCommand.process("@history", senderUser, deskRoom, deskDirectory);
	}

	@Test
	public void handle_RequestHistoryWithParamMoreThanExpected_ResponseHelpMessage()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				MessageResponse.createDeskMessageResponse("@history <nickname> <#page>", (User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryByMemberCommand.process("@history user1 1 2", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestHistoryOfUserThatIsNotInDesk_ResponseErrorMessage()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				deskRoom.getOccupantByNickname(anyString); result = null;
				
				MessageResponse.createDeskMessageResponse("Cannot retrieve history for user1 because user is not currently in desk.", (User)any, (JID)any); times = 1;
			}
		};
		
		getHistoryByMemberCommand.process("@history user1", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestHistoryOfUserWithNoPageSpecified_RequestHistoryWithOffsetZero()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				deskRoom.getOccupantByNickname(anyString); result = UserCreator.createMemberUser("user1");
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 0, 10); times = 1; result = searchResult;
				
				searchResult.setTotalResult(0);
			}
		};
		
		getHistoryByMemberCommand.process("@history user1", senderUser, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_RequestHistoryOfUserWithPageSpecified_RequestHistoryWithOffsetZero()
	{
		new Expectations()
		{
			@Mocked @NonStrict MessageResponse messageResponse;
			{
				serviceConfiguration.isHistorySearchEnabled(); result = true;
				
				deskRoom.getOccupantByNickname(anyString); result = UserCreator.createMemberUser("user1");
				
				serviceConfiguration.getHistoryPageSize(); result = 10;
				
				historySearcher.search(anyString, anyString, 10, 10); times = 1; result = searchResult;
				
				searchResult.setTotalResult(0);
			}
		};
		
		getHistoryByMemberCommand.process("@history user1 2", senderUser, deskRoom, deskDirectory);
	}
}
