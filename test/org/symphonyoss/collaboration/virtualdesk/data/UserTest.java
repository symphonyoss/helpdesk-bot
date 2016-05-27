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

package org.symphonyoss.collaboration.virtualdesk.data;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;

public class UserTest
{
	private User user;
	
	@Before
	public void before()
	{
		user = new User(JIDUtils.getUserJID("user1"), "nickname1");
	}
	
	@Test
	public void constructor_NewInstanceWithTwoParams_SetPropertiesFromParams()
	{
		Assert.assertEquals("user1", user.getJID().getNode());
		Assert.assertEquals("nickname1", user.getNickname());
		Assert.assertEquals(PresenceType.Online, user.getPresence());
	}
	
	@Test
	public void constructor_NewInstanceWithThreeParams_SetPropertiesFromParams()
	{
		User user2 = new User(JIDUtils.getUserJID("user2"), "nickname2", Affiliation.none, Role.moderator);
		
		Assert.assertEquals("user2", user2.getJID().getNode());
		Assert.assertEquals("nickname2", user2.getNickname());
		Assert.assertEquals(PresenceType.Online, user2.getPresence());
		Assert.assertEquals(Affiliation.none, user2.getAffiliation());
		Assert.assertEquals(Role.moderator, user2.getRole());
	}
	
	@Test
	public void setJid_AnyConditions_CanGetJIDBackWithSameValue()
	{
		JID userJID = JIDUtils.getUserJID("user3");
		
		user.setJid(userJID, "nickname3");
		
		Assert.assertEquals(userJID, user.getJID());
		Assert.assertEquals("nickname3", user.getNickname());
	}
	
	@Test
	public void setNickname_AnyConditions_CanGetNicknameBackWithSameValue()
	{
		user.setNickname("abc");
		
		Assert.assertEquals("abc", user.getNickname());
	}
	
	@Test
	public void setAffiliation_AnyConditions_CanGetAffiliationBackWithSameValue()
	{
		user.setAffiliation(Affiliation.member);
		
		Assert.assertEquals(Affiliation.member, user.getAffiliation());
	}
	
	@Test
	public void setRole_AnyConditions_CanGetRoleBackWithSameValue()
	{
		user.setRole(Role.participant);
		
		Assert.assertEquals(Role.participant, user.getRole());
	}
	
	@Test
	public void setPresence_AnyConditions_CanGetPresenceBackWithSameValue()
	{
		user.setPresence(PresenceType.FreeToChat);
		
		Assert.assertEquals(PresenceType.FreeToChat, user.getPresence());
	}
	
	@Test
	public void isDeskMember_UserIsOwner_ReturnTrue()
	{
		user.setAffiliation(Affiliation.owner);
		
		Assert.assertTrue(user.isDeskMember());
	}
	
	@Test
	public void isDeskMember_UserIsAdmin_ReturnTrue()
	{
		user.setAffiliation(Affiliation.admin);
		
		Assert.assertTrue(user.isDeskMember());
	}
	
	@Test
	public void isDeskMember_UserIsMember_ReturnTrue()
	{
		user.setAffiliation(Affiliation.member);
		
		Assert.assertTrue(user.isDeskMember());
	}
	
	@Test
	public void isDeskMember_UserIsParticipant_ReturnFalse()
	{
		user.setAffiliation(Affiliation.none);
		
		Assert.assertFalse(user.isDeskMember());
	}
}
