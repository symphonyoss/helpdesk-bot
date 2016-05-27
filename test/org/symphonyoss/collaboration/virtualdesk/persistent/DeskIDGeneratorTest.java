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

import javax.sql.DataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskIDGenerator;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DeskIDGeneratorTest
{
	private static final int TYPE_ID = 99; 
	
	private DeskIDGenerator idGenerator;
	

	
	@Before
	public void before()
	{
		idGenerator = new DeskIDGenerator(TYPE_ID);
	}
	
	@Test
	public void getNextDeskID_NoIDReturned_CreateIDStartWithOneAndReturnOne()
	{
		new Expectations()
		{
			@Mocked @NonStrict JdbcTemplate jdbcTemplate;
			@Mocked DataSource dataSource;
			{
				idGenerator.setDataSource(dataSource);
				
				jdbcTemplate.queryForInt(anyString, any); result = new IncorrectResultSizeDataAccessException(1);
				
				// Expect to create the new record in database with start id = 1
				jdbcTemplate.update(anyString, TYPE_ID, 1); times = 1;
			}
		};
		
		int id = idGenerator.getNextDeskID();
		
		Assert.assertEquals(1, id);
	}
	
	@Test
	public void getNextDeskID_HaveIDReturned_ReturnIDFromDatabase()
	{
		final int id = 4;
		
		new Expectations()
		{
			@Mocked @NonStrict JdbcTemplate jdbcTemplate;
			@Mocked DataSource dataSource;
			{
				idGenerator.setDataSource(dataSource);
				
				jdbcTemplate.queryForInt(anyString, any); result = id;
				
				// Act as successfully update the new id to database
				jdbcTemplate.update(anyString, id + 1, TYPE_ID, id); result = 1;
			}
		};
		
		int returnedID = idGenerator.getNextDeskID();
		
		Assert.assertEquals(id + 1, returnedID);
	}
	
	@Test
	public void getNextDeskID_SomeoneTakeIDWhileUpdatingNewIDToDatabase_ReturnMinusOne()
	{
		final int id = 8;
		
		new Expectations()
		{
			@Mocked @NonStrict JdbcTemplate jdbcTemplate;
			@Mocked DataSource dataSource;
			{
				idGenerator.setDataSource(dataSource);
				
				jdbcTemplate.queryForInt(anyString, any); result = id;
				
				// Act as successfully update the new id to database
				jdbcTemplate.update(anyString, id + 1, TYPE_ID, id); result = 0;
			}
		};
		
		int returnedID = idGenerator.getNextDeskID();
		
		Assert.assertEquals(-1, returnedID);
	}
}
