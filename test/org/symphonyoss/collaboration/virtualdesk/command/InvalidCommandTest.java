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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

;
public class InvalidCommandTest
{
	private User senderUser;
	private Desk deskRoom;

	@Before
	public void before()
	{
		senderUser = UserCreator.createOwnerUser("Admin.User01");
		deskRoom = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);
	}

	@Test
	public void process_AnyConditions_ReturnInvalidMessage()
	{
		InvalidCommand invalidCommand = new InvalidCommand();

		Collection <Packet> packetList = invalidCommand.process("", senderUser, deskRoom, null);

		Message message = (Message) packetList.iterator().next();

		Assert.assertEquals(1, packetList.size());
		Assert.assertEquals(deskRoom.getJID(), message.getFrom());
		Assert.assertEquals(senderUser.getJID(), message.getTo());
		Assert.assertEquals(Type.groupchat, message.getType());
		Assert.assertEquals("Invalid command.", message.getBody());
	}
}
