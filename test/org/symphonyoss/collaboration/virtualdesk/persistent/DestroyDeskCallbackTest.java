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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.persistence.DestroyDeskCallback;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DestroyDeskCallbackTest
{
	private DestroyDeskCallback destroyDeskCallback;
	private @Mocked @NonStrict Desk deskRoom;
	private @Mocked @NonStrict IServiceConfiguration serviceConfiguration;
	private @Mocked @NonStrict JdbcTemplate jdbcTemplate;

	
	@Before
	public void before()
	{
		destroyDeskCallback = new DestroyDeskCallback(deskRoom, serviceConfiguration, jdbcTemplate);
	}
	
	@Test
	public void doInTransactionWithoutResult_DeleteDeskFromDatabaseAndSetIDToMinusOne()
	{
		new Expectations()
		{
			{			
				jdbcTemplate.update(anyString, any, any); times = 3;
				
				deskRoom.setID(-1); times = 1;
			}
		};
		
		destroyDeskCallback.doInTransaction(null);
	}
	
	@Test
	public void doInTransactionWithoutResult_DeleteDeskFromDatabase_ReturnGenericException()
	{
		new Expectations()
		{
			final Exception exception = new Exception("Database Exception");
			{			
				jdbcTemplate.update(anyString, any); result = exception ;
			}
		};
		
		try
		{
			destroyDeskCallback.doInTransaction(null);
		}
		catch (Exception ex)
		{
			Assert.assertTrue(ex instanceof Exception);
		}
	}
	
}
