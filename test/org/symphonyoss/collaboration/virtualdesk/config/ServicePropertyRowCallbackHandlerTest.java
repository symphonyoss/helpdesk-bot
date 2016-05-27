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

package org.symphonyoss.collaboration.virtualdesk.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class ServicePropertyRowCallbackHandlerTest
{
	private ServicePropertyRowCallbackHandler rowHandler;

	private IServiceConfiguration serviceConfiguration;

	private @Mocked @NonStrict JdbcTemplate jdbcTemplate;
	private @Mocked @NonStrict ResultSet resultSet;

	@Before
	public void before()
	{
		// Don't care about datasource because we've already mocked jdbcTemplate
		serviceConfiguration = new ServiceConfiguration("virtualdesk", null);
		
		rowHandler = new ServicePropertyRowCallbackHandler(serviceConfiguration);
	}

	@Test
	public void processRow_NoMaxNumberOfDeskCreatedPropertyReturn_UseDefaultValue() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("name"); result = "NoProperty";
				resultSet.getString("propValue"); result = "50";
			}
		};

		rowHandler.processRow(resultSet);

		Assert.assertEquals(ServiceConfiguration.DEFAULT_MAXNUMBER_OF_DESKCREATED, serviceConfiguration.getMaxNumberOfDeskCreated());
	}

	@Test
	public void processRow_HaveMaxNumberOfDeskCreatedPropertyReturn_SetValueToServiceConfiguration() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("name"); result = ServiceConfiguration.MAXNUMBER_OF_DESKCREATED;
				resultSet.getString("propValue"); result = "50";
			}
		};

		rowHandler.processRow(resultSet);

		Assert.assertEquals(50, serviceConfiguration.getMaxNumberOfDeskCreated());
	}

	@Test
	public void processRow_HaveQuestionReceivedMessagePropertyReturn_SetValueToServiceConfiguration() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("name"); result = ServiceConfiguration.QUESTION_RECEIVED_MESSAGE;
				resultSet.getString("propValue"); result = "Test message";
			}
		};

		rowHandler.processRow(resultSet);

		Assert.assertEquals("Test message", serviceConfiguration.getQuestionReceivedMessage());
	}
	
	@Test
	public void processRow_HaveNumberOfRecentQuestionsPropertyReturn_SetValueToServiceConfiguration() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("name"); result = ServiceConfiguration.NUMBER_OF_RECENTQUESTIONS;
				resultSet.getString("propValue"); result = "9";
			}
		};
		
		rowHandler.processRow(resultSet);
		
		Assert.assertEquals(9, serviceConfiguration.getNumberOfRecentQuestions());
	}
	
	@Test
	public void processRow_HaveHistoryPageSizePropertyReturn_SetValueToServiceConfiguration() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getString("name"); result = ServiceConfiguration.HISTORY_PAGESIZE;
				resultSet.getString("propValue"); result = "3";
			}
		};
		
		rowHandler.processRow(resultSet);
		
		Assert.assertEquals(3, serviceConfiguration.getHistoryPageSize());
	}
}
