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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.Packet;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class GetDeskFormHandlerTest
{
	private GetDeskFormHandler getDeskFormHandler;
	
	private IQ iqRequest;
	
	private @Mocked @NonStrict Desk deskRoom;
	
	@Before
	public void before()
	{
		iqRequest = new IQ();
		iqRequest.setFrom(JIDUtils.getUserJID("Sender1"));
		iqRequest.setTo(JIDUtils.getUserJID("Receiver1"));
		
		iqRequest.setChildElement("query", Namespace.MUC_OWNER);
		
		getDeskFormHandler = new GetDeskFormHandler(iqRequest, deskRoom);
	}
	
	@Test
	public void handle_UserIsNotDeskOwner_ReturnIQError()
	{
		new Expectations()
		{
			{
				Set<String> ownerSet = new HashSet<String>();
				ownerSet.add("User1");
				
				deskRoom.getOwners(); result = ownerSet;
			}
		};
		
		List<Packet> packetList = (List<Packet>)getDeskFormHandler.handle();
		
		Assert.assertEquals(1, packetList.size());
		Assert.assertEquals(Type.error, ((IQ)packetList.get(0)).getType());
	}
	
	@Test
	public void handle_UserIsDeskOwner_CallCreateGetDeskForm()
	{
		new Expectations()
		{
			@Mocked @NonStrict IQResponse iqResponse;
			{
				Set<String> ownerSet = new HashSet<String>();
				ownerSet.add(JIDUtils.getUserBareJID("sender1"));
				
				deskRoom.getOwners(); result = ownerSet;
				
				IQResponse.createGetDeskForm(iqRequest, deskRoom); times = 1;
			}
		};
		
		getDeskFormHandler.handle();
	}
}
