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

package org.symphonyoss.collaboration.virtualdesk.persistent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.serializer.QuestionSerializer;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPropertiesLoader;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DeskPropertiesLoaderTest
{
	private DeskPropertiesLoader loader;

	private Desk desk;
	
	private @Mocked @NonStrict ResultSet resultSet;

	@Before
	public void before()
	{
		desk = new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN);

		List <Desk> deskList = new ArrayList <Desk>();
		deskList.add(desk);

		loader = new DeskPropertiesLoader(deskList);
	}

	@Test
	public void processRow_NoDeskInList_DoNothing() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk2";
				
				resultSet.getString("name"); times = 0;
			}
		};

		loader.processRow(resultSet);
	}

	@Test
	public void processRow_PropertyDoesNotMatch_DoNothing() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "NotMatchedProperty";
				
				resultSet.getString("propValue"); times = 0;
			}
		};

		loader.processRow(resultSet);
	}

	@Test
	public void processRow_ContainsCreatorProperty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "Creator";
				
				resultSet.getString("propValue"); result = "User1";
			}
		};

		loader.processRow(resultSet);

		Assert.assertEquals("User1", desk.getCreator());
	}
	
	@Test
	public void processRow_ContainsDeskOwnersPropertyButValueIsNull_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskOwners";
				
				resultSet.getString("propValue"); result = null;
			}
		};
		
		loader.processRow(resultSet);
		
		Set <String> ownerSet = desk.getOwners();
		
		Assert.assertEquals(0, ownerSet.size());
	}

	@Test
	public void processRow_ContainsDeskOwnersPropertyButValueIsEmpty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskOwners";
				
				resultSet.getString("propValue"); result = "";
			}
		};

		loader.processRow(resultSet);

		Set <String> ownerSet = desk.getOwners();

		Assert.assertEquals(0, ownerSet.size());
	}

	@Test
	public void processRow_ContainsDeskOwnersPropertyButValueHasOnlyComma_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskOwners";
				
				resultSet.getString("propValue"); result = ",";
			}
		};

		loader.processRow(resultSet);

		Set <String> ownerSet = desk.getOwners();

		Assert.assertEquals(0, ownerSet.size());
	}

	@Test
	public void processRow_ContainsDeskOwnersProperty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskOwners";
				
				resultSet.getString("propValue"); result = "User1,User2";
			}
		};

		loader.processRow(resultSet);

		Set <String> ownerSet = desk.getOwners();

		Assert.assertEquals(2, ownerSet.size());
		Assert.assertTrue(ownerSet.contains("User1"));
		Assert.assertTrue(ownerSet.contains("User2"));
	}

	@Test
	public void processRow_ContainsDeskAdminsProperty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskAdmins";
				
				resultSet.getString("propValue"); result = "User1,User2";
			}
		};

		loader.processRow(resultSet);

		Set <String> adminSet = desk.getAdmins();

		Assert.assertEquals(2, adminSet.size());
		Assert.assertTrue(adminSet.contains("User1"));
		Assert.assertTrue(adminSet.contains("User2"));
	}
	
	@Test
	public void processRow_ContainsDeskMembersProperty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskMembers";
				
				resultSet.getString("propValue"); result = "User1,User2";
			}
		};
		
		loader.processRow(resultSet);
		
		Set <String> memberSet = desk.getMembers();
		
		Assert.assertEquals(2, memberSet.size());
		Assert.assertTrue(memberSet.contains("User1"));
		Assert.assertTrue(memberSet.contains("User2"));
	}

	@Test
	public void processRow_ContainsDeskParticipantsProperty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskParticipants";
				
				resultSet.getString("propValue"); result = "User1,User2";
			}
		};

		loader.processRow(resultSet);

		Set <String> participantSet = desk.getParticipants();

		Assert.assertEquals(2, participantSet.size());
		Assert.assertTrue(participantSet.contains("User1"));
		Assert.assertTrue(participantSet.contains("User2"));
	}
	
	@Test
	public void processRow_ContainsDeskAliasProperty_SetPropertiesToDesk() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskAlias";
				
				resultSet.getString("propValue"); result = "Desk_Support";
			}
		};
		
		loader.processRow(resultSet);
		
		Assert.assertEquals("Desk_Support", desk.getDeskAliasName());
	}
	
	@Test
	public void processRow_CannotParsePropertyValue_SkipThatPropertyAndNoExceptionThrown() throws SQLException
	{
		new Expectations()
		{
			@Mocked @NonStrict
			QuestionSerializer questionSerializer;
			{
				resultSet.getString("deskName"); result = "Desk1";
				
				resultSet.getString("name"); result = "DeskParticipants";
				
				resultSet.getString("propValue"); result = new Exception();
			}
		};
		
		loader.processRow(resultSet);
	}
}
