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

package org.symphonyoss.collaboration.virtualdesk.packet;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;

public class PacketUtilsTest
{
	@Before
	public void before()
	{
		new PacketUtils();
	}
	
	@Test
	public void getIQNamespace_AnyConditions_ReturnIQNamespace()
	{
		IQ iq = new IQ();
		iq.setChildElement("query", Namespace.MUC_ROOM_CONFIG);
		
		String namespace = PacketUtils.getIQNamespace(iq);
		
		Assert.assertEquals(Namespace.MUC_ROOM_CONFIG, namespace);
	}
	
	@Test
	public void hasNamespace_DifferentNamespace_ReturnFalse()
	{
		Message message = new Message();
		message.addChildElement("x", Namespace.DISCO_INFO);
		
		boolean hasNamespace = PacketUtils.hasNamespace(message, Namespace.MUC_ADMIN);
		
		Assert.assertFalse(hasNamespace);
	}
	
	@Test
	public void hasNamespace_SameNamespace_ReturnTrue()
	{
		Message message = new Message();
		message.addChildElement("x", Namespace.XDATA);
		
		boolean hasNamespace = PacketUtils.hasNamespace(message, Namespace.XDATA);
		
		Assert.assertTrue(hasNamespace);
	}
}
