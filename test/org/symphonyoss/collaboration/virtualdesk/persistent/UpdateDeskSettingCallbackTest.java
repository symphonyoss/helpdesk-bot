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

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xmpp.packet.JID;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;

import org.symphonyoss.collaboration.virtualdesk.persistence.DeskIDGenerator;
import org.symphonyoss.collaboration.virtualdesk.persistence.UpdateDeskSettingCallback;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class UpdateDeskSettingCallbackTest
{
	private UpdateDeskSettingCallback updateDeskCallback;
	private @Mocked @NonStrict Desk deskRoom;
	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	private @Mocked @NonStrict JdbcTemplate jdbcTemplate;
	private @Mocked @NonStrict DeskIDGenerator idGenerator;
	

	
	@Before
	public void before()
	{
		updateDeskCallback = new UpdateDeskSettingCallback(deskRoom, serviceConfiguration, jdbcTemplate, idGenerator);
	}
	
	@Test
	public void doInTransactionWithoutResult_DeskIsNotPersistent_DeleteDeskFromDatabaseAndSetIDToMinusOne()
	{
		new Expectations()
		{
			{
				deskRoom.isPersistent(); result = false;
				
				jdbcTemplate.update(anyString, any, any); times = 3;
				
				deskRoom.setID(-1); times = 1;
			}
		};
		
		updateDeskCallback.doInTransaction(null);
	}
	
	@Test
	public void doInTransactionWithoutResult_DeskIsPersistentAndAlreadyPersistedBefore_UpdateDeskSettingInDatabase()
	{
		new Expectations()
		{
			{
				int deskID = 1;
				
				deskRoom.isPersistent(); result = true;
				deskRoom.getID(); result = deskID;
				
				jdbcTemplate.update(anyString, any, any, any, any, any, deskID); times = 1;

				jdbcTemplate.update(anyString, any, deskID, "DeskOwners", any); times = 1;
				jdbcTemplate.update(anyString, any, deskID, "DeskAdmins", any); times = 1;
				jdbcTemplate.update(anyString, any, deskID, "DeskParticipants", any); times = 1;
				jdbcTemplate.update(anyString, any, deskID, "DeskAlias", any); times = 1;

			}
		};
		
		updateDeskCallback.doInTransaction(null);
	}
	
	@Test
	public void doInTransactionWithoutResult_DeskIsPersistentButNeverPersist_InsertDeskSettingInDatabase()
	{
		new Expectations()
		{
			{
				int newDeskID = 4;

				deskRoom.isPersistent(); result = true;
				deskRoom.getID(); returns(-1, newDeskID);
				
				idGenerator.getNextDeskID(); result = newDeskID;
				
				// Expect the desk has to be set the generated desk ID
				deskRoom.setID(newDeskID); times = 1;
				
				deskRoom.getJID(); result = new JID("User1", "domain.com", null);
				
				// Expect desk setting has to insert with generated desk ID
				jdbcTemplate.update(anyString, any, newDeskID, any, any, any, any, any, any); times = 1;

				jdbcTemplate.update(anyString, any, newDeskID, "Creator", any); times = 1;

				jdbcTemplate.update(anyString, any, newDeskID, "DeskOwners", any); times = 1;
				jdbcTemplate.update(anyString, any, newDeskID, "DeskAdmins", any); times = 1;
				jdbcTemplate.update(anyString, any, newDeskID, "DeskParticipants", any); times = 1;
				jdbcTemplate.update(anyString, any, newDeskID, "DeskAlias", any); times = 1;
			}
		};
		
		updateDeskCallback.doInTransaction(null);
	}
}
