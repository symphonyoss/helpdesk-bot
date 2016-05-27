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

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.symphonyoss.collaboration.virtualdesk.command.AbstractCommand;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class MemberMessageReceivedHandlerTest
{
	private AbstractActionHandler messageReceivedHandler;
	
	private Message message;
	
	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	
	private @Mocked @NonStrict Desk deskRoom;
	
	private @Mocked @NonStrict MessageResponse messageResponse;
	
	private @Mocked @NonStrict AbstractCommand command;
	
	private @Mocked @NonStrict IDeskDirectory deskDirectory; 

	@Before
	public void before()
	{
		message = new Message();
		
		message.setFrom(JIDUtils.getUserJID("user1"));
		message.setTo(JIDUtils.getNicknameJID("desk1", null));
		message.setBody("test");

		messageReceivedHandler = new MemberMessageReceivedHandler(serviceConfiguration, message, deskRoom, deskDirectory);
	}
	
	@Test
	public void handle_MessageBodyIsCommand_CallProcessCommand()
	{
		message.setBody("@command");
		
		new Expectations()
		{
			{
				command.process(anyString, (User)any, (Desk)any, null); times = 1;
			}
		};
		
		messageReceivedHandler.handle();
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void handle_MessageBodyIsNormalText_ResponseMessageToDeskMembers()
	{
		new Expectations()
		{
			@Mocked MessageResponse messageResponse;
			{
				deskRoom.getCurrentMembers(); times = 1;
				
				MessageResponse.createMessageResponse((User)any, (Collection<User>)any, (JID)any, message);
			}
		};
		
		messageReceivedHandler.handle();
	}
}
