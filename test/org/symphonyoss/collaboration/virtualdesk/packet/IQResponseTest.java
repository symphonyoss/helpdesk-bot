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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

public class IQResponseTest
{
	private IQ iq;
	
	@Before
	public void before()
	{
		new IQResponse();
		
		iq = new IQ();
		iq.setFrom(JIDUtils.getUserJID("user1"));
		iq.setTo(JIDUtils.getUserJID("user2"));
	}
	
	@Test
	public void createErrorResponse_AnyConditions_ReturnIQError()
	{
		iq.setChildElement("query", Namespace.DISCO_INFO);
		
		IQ response = (IQ)IQResponse.createErrorResponse(iq, PacketError.Condition.feature_not_implemented);
		
		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertEquals(IQ.Type.error, response.getType());
		Assert.assertEquals(PacketError.Condition.feature_not_implemented, response.getError().getCondition());
	}
	
	@Test
	public void createRoomListing_AnyConditions_ReturnIQRoomListing()
	{
		iq.setChildElement("query", Namespace.DISCO_ITEMS);
		
		Desk desk1 = new Desk("desk1", TestConst.VIRTUALDESK_DOMAIN);
		desk1.setNaturalName("desk1_natural");
		Desk desk2 = new Desk("desk2", TestConst.VIRTUALDESK_DOMAIN);
		desk2.setNaturalName("desk2_natural");
		
		List<Desk> deskList = new ArrayList<Desk>();
		deskList.add(desk1);
		deskList.add(desk2);
		
		IQ response = (IQ)IQResponse.createRoomListing(iq, deskList);
		
		Element queryElement = response.getChildElement();
		
		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertEquals(2, queryElement.elements("item").size());
		Assert.assertEquals(desk1.getJID().toBareJID(), ((Element)queryElement.elements("item").get(0)).attribute("jid").getText());
		Assert.assertEquals(desk1.getNaturalName(), ((Element)queryElement.elements("item").get(0)).attribute("name").getText());
		Assert.assertEquals(desk2.getJID().toBareJID(), ((Element)queryElement.elements("item").get(1)).attribute("jid").getText());
		Assert.assertEquals(desk2.getNaturalName(), ((Element)queryElement.elements("item").get(1)).attribute("name").getText());
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void createGetDeskForm_PersistentAndMembersOnlySettingsAreTrue_ReturnIQDeskFormWithBothSettingsAreOne()
	{
		iq.setChildElement("query", Namespace.DISCO_INFO);
		
		Desk desk1 = new Desk("desk1", TestConst.VIRTUALDESK_DOMAIN);
		
		desk1.setDescription("Test desk");
		desk1.isMembersOnly(true);
		desk1.addOwner("user1");
		desk1.addOwner("user2");
		desk1.addAdmin("user1");
		desk1.addAdmin("user2");
		desk1.addMember("user3");
		desk1.addMember("user4");
		desk1.addParticipants("user1");
		desk1.addParticipants("user2");
		
		IQ response = (IQ)IQResponse.createGetDeskForm(iq, desk1);
		
		Element xElement = response.getChildElement().element(new QName("x", new org.dom4j.Namespace("", Namespace.XDATA)));
		
		List<Element> fields = xElement.elements("field");
		
		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertEquals(desk1.getName(), fields.get(1).element("value").getText());			// Desk name
		Assert.assertEquals(desk1.getDescription(), fields.get(2).element("value").getText());	// Description
		Assert.assertEquals(desk1.getDeskAliasName(), fields.get(3).element("value").getText());	// Desk Alias
		Assert.assertEquals("1", fields.get(4).element("value").getText());						// Persistent
		Assert.assertEquals("1", fields.get(5).element("value").getText());						// Members only
		Assert.assertEquals("user1,user2", fields.get(6).element("value").getText());			// Desk participant
		Assert.assertEquals("user3,user4", fields.get(7).element("value").getText());			// Desk member
		Assert.assertEquals("user1,user2", fields.get(8).element("value").getText());			// Desk admin
		Assert.assertEquals("user1,user2", fields.get(9).element("value").getText());			// Desk owner
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void createGetDeskForm_PersistentAndMembersOnlySettingsAreFalse_ReturnIQDeskFormWithBothSettingsAreZero()
	{
		iq.setChildElement("query", Namespace.DISCO_INFO);
		
		Desk desk1 = new Desk("desk2", TestConst.VIRTUALDESK_DOMAIN);
		
		desk1.setDescription("Test description");
		desk1.isMembersOnly(false);
		desk1.addOwner("user4");
		desk1.addOwner("user3");
		desk1.addAdmin("user5");
		desk1.addAdmin("user6");
		desk1.addMember("user10");
		desk1.addMember("user11");
		desk1.addParticipants("user8");
		desk1.addParticipants("user9");
		
		IQ response = (IQ)IQResponse.createGetDeskForm(iq, desk1);
		
		Element xElement = response.getChildElement().element(new QName("x", new org.dom4j.Namespace("", Namespace.XDATA)));
		
		List<Element> fields = xElement.elements("field");
		
		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertEquals(desk1.getName(), fields.get(1).element("value").getText());			// Desk name
		Assert.assertEquals(desk1.getDescription(), fields.get(2).element("value").getText());	// Description
		Assert.assertEquals(desk1.getDeskAliasName(), fields.get(3).element("value").getText());	// Desk Alias
		Assert.assertEquals("1", fields.get(4).element("value").getText());						// Persistent
		Assert.assertEquals("0", fields.get(5).element("value").getText());						// Members only
		Assert.assertEquals("user8,user9", fields.get(6).element("value").getText());			// Desk participant
		Assert.assertEquals("user10,user11", fields.get(7).element("value").getText());			// Desk admin
		Assert.assertEquals("user5,user6", fields.get(8).element("value").getText());			// Desk admin
		Assert.assertEquals("user4,user3", fields.get(9).element("value").getText());			// Desk owner
	}
	
	@Test
	public void createGetOccupantList_AnyConditions_ReturnIQOccupantList()
	{
		iq.setTo(JIDUtils.getNicknameJID("desk1", "abc"));
		iq.setChildElement("query", Namespace.DISCO_ITEMS);
		
		User user1 = new User(JIDUtils.getUserJID("user1"), "user1");
		User user2 = new User(JIDUtils.getUserJID("user2"), "user2");
		
		List<User> occupantList = new ArrayList<User>();
		occupantList.add(user1);
		occupantList.add(user2);
		
		IQ response = (IQ)IQResponse.createGetOccupantList(iq, occupantList);
		
		Element queryElement = response.getChildElement();

		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertEquals(2, queryElement.elements("item").size());
		Assert.assertEquals(JIDUtils.getNicknameJID("desk1", user1.getNickname()).toFullJID(), ((Element)queryElement.elements("item").get(0)).attribute("jid").getText());
		Assert.assertEquals(JIDUtils.getNicknameJID("desk1", user2.getNickname()).toFullJID(), ((Element)queryElement.elements("item").get(1)).attribute("jid").getText());
	}
	
	@Test
	public void createDeskInfo_AnyConditions_ReturnIQDeskInfo()
	{
		iq.setChildElement("query", Namespace.DISCO_INFO);
		
		IQ response = (IQ)IQResponse.createDeskInfo(iq, "desk4", "desc", 5);
		
		Element xElement = response.getChildElement().element(new QName("x", new org.dom4j.Namespace("", Namespace.XDATA)));
		
		XPath numOfOccupantXPath = xElement.createXPath("//x:field[@var='muc#roominfo_occupants']/x:value");

		Map <String, String> namespaceUris = new HashMap <String, String>();
		namespaceUris.put("x", Namespace.XDATA);

		numOfOccupantXPath.setNamespaceURIs(namespaceUris);
		
		Node numOfOccupantElement = numOfOccupantXPath.selectSingleNode(xElement);

		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertEquals("5", numOfOccupantElement.getText());
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void createVirtualDeskServiceInfo_AnyConditions_ReturnIQServiceInfo()
	{
		iq.setChildElement("query", Namespace.DISCO_INFO);

		IQ response = (IQ)IQResponse.createVirtualDeskServiceInfo(iq);

		Element queryElement = response.getChildElement();
		
		List<Element> identityNodes = (List<Element>)queryElement.elements("identity");
		List<Element> featuerNodes = (List<Element>)queryElement.elements("feature");
		
		Assert.assertEquals(iq.getTo(), response.getFrom());
		Assert.assertEquals(iq.getFrom(), response.getTo());
		Assert.assertTrue(identityNodes.size() > 0);
		Assert.assertTrue(featuerNodes.size() > 0);
	}
}
