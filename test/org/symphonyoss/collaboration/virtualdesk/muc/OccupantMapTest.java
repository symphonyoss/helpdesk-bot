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

package org.symphonyoss.collaboration.virtualdesk.muc;

import java.util.Collection;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;

public class OccupantMapTest
{
	private OccupantMap occupantMap;

	private User returnUser;
	private User user1;
	private User user2;

	@Before
	public void before()
	{
		occupantMap = new OccupantMap();

		user1 = UserCreator.createParticipantUser("user1");
		user2 = UserCreator.createParticipantUser("user2");
	}

	@Test
	public void constructor_CreateNewOccupantMap_OccupantCountEqualsZero()
	{
		Assert.assertEquals(0, occupantMap.getOccupantCount());
	}

	@Test
	public void addOccupant_AddOneOccupant_OccupantCountEqualsOne()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		Assert.assertEquals(1, occupantMap.getOccupantCount());
	}

	@Test
	public void addOccupant_AddTwoOccupants_OccupantCountEqualsTwo()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);
		occupantMap.addOccupant(user2.getJID().toBareJID(), user2.getNickname(), user2);

		Assert.assertEquals(2, occupantMap.getOccupantCount());
	}

	@Test
	public void getUserByJID_NoOccupant_ReturnNull()
	{
		User returnUser = occupantMap.getUserByJID(user1.getJID().toBareJID());

		Assert.assertNull(returnUser);
	}

	@Test
	public void getUserByJID_HaveRequestedOccupant_ReturnThatOccupant()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		User returnUser = occupantMap.getUserByJID(user1.getJID().toBareJID());

		Assert.assertSame(user1, returnUser);
	}

	@Test
	public void getUserByJID_HaveOneOccupantButGetNotExistedOccupant_ReturnNull()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		User returnUser = occupantMap.getUserByJID(user2.getJID().toBareJID());

		Assert.assertNull(returnUser);
	}

	@Test
	public void getUserByNickname_NoOccupant_ReturnNull()
	{
		User returnUser = occupantMap.getUserByNickname(user1.getNickname());

		Assert.assertNull(returnUser);
	}

	@Test
	public void getUserByNickname_HaveRequestedOccupant_ReturnThatOccupant()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		User returnUser = occupantMap.getUserByNickname(user1.getNickname());

		Assert.assertSame(user1, returnUser);
	}

	@Test
	public void getUserByNickname_HaveOneOccupantButGetNotExistedOccupant_ReturnNull()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		User returnUser = occupantMap.getUserByNickname(user2.getNickname());

		Assert.assertNull(returnUser);
	}

	@Test
	public void getOccupants_NoOccupant_ReturnEmptyCollectionOfUser()
	{
		Collection <User> userCollection = occupantMap.getOccupants();

		Assert.assertEquals(0, userCollection.size());
	}

	@Test
	public void getOccupants_HaveTwoOccupants_ReturnCollectionOfUserThatContainsThoseTwoUsers()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);
		occupantMap.addOccupant(user2.getJID().toBareJID(), user2.getNickname(), user2);

		Collection <User> userCollection = occupantMap.getOccupants();

		Assert.assertEquals(2, userCollection.size());
		Assert.assertTrue(userCollection.contains(user1));
		Assert.assertTrue(userCollection.contains(user2));
	}

	@Test
	public void removeOccupantByJID_EmptyOccupant_DoNothing()
	{
		occupantMap.removeOccupantByJID(user2.getJID().toBareJID());

		Assert.assertEquals(0, occupantMap.getOccupantCount());
	}

	@Test
	public void removeOccupantByJID_RemoveNotExistedUser_DoNothing()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		occupantMap.removeOccupantByJID(user2.getJID().toBareJID());

		Assert.assertEquals(1, occupantMap.getOccupantCount());
	}

	@Test
	public void removeOccupantByJID_HaveRemovedOccupant_OccupantCountIsDecreased()
	{
		occupantMap.addOccupant(user1.getJID().toBareJID(), user1.getNickname(), user1);

		occupantMap.removeOccupantByJID(user1.getJID().toBareJID());

		Assert.assertEquals(0, occupantMap.getOccupantCount());
	}
}
