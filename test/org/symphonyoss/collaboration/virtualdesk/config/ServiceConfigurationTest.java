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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import javax.sql.DataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.util.ReflectionTestUtils;

public class ServiceConfigurationTest
{
	private ServiceConfiguration serviceConfiguration;

	private JdbcTemplate mockJdbcTemplate;

	@Before
	public void before()
	{
		DataSource mockDataSource = mock(DataSource.class);

		serviceConfiguration = new ServiceConfiguration("virtualdesk", mockDataSource);

		mockJdbcTemplate = mock(JdbcTemplate.class);

		ReflectionTestUtils.setField(serviceConfiguration, "jdbcTemplate", mockJdbcTemplate);
	}

	@Test
	public void constructor_AnyConditions_SetDefaultValueToFields()
	{
		Assert.assertEquals(ServiceConfiguration.DEFAULT_SERVICEID, serviceConfiguration.getServiceID());
		Assert.assertEquals(ServiceConfiguration.DEFAULT_MAXNUMBER_OF_DESKCREATED, serviceConfiguration.getMaxNumberOfDeskCreated());
		Assert.assertEquals(ServiceConfiguration.DEFAULT_NUMBER_OF_RECENTQUESTIONS, serviceConfiguration.getNumberOfRecentQuestions());
		Assert.assertEquals(ServiceConfiguration.DEFAULT_QUESTION_RECEIVED_MESSAGE, serviceConfiguration.getQuestionReceivedMessage());
		Assert.assertEquals(ServiceConfiguration.DEFAULT_HISTORY_PAGESIZE, serviceConfiguration.getHistoryPageSize());
		
		Assert.assertFalse(serviceConfiguration.isHistorySearchEnabled());
	}

	@Test
	public void init_SetServiceIDToMinusOne_NotCallQueryDatabaseToGetServiceProperties()
	{
		serviceConfiguration.setServiceID(ServiceConfiguration.NO_SERVICEID);
		
		serviceConfiguration.init();

		verify(mockJdbcTemplate, times(0)).query(anyString(), (Object[]) any(), (RowCallbackHandler) any());
	}

	@Test
	public void init_HaveServiceIDRegisteredInDatabase_CallQueryDatabaseOnceToGetServiceProperties()
	{
		serviceConfiguration.setServiceID(1);

		serviceConfiguration.init();

		verify(mockJdbcTemplate, times(1)).query(anyString(), (Object[]) any(), (RowCallbackHandler) any());
	}
	
	@Test
	public void setVirtualDeskSubDomain_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setVirtualDeskSubDomain("virtualdesk_sub_domain");
		
		Assert.assertEquals("virtualdesk_sub_domain", serviceConfiguration.getVirtualDeskSubDomain());
	}
	
	@Test
	public void setServiceDescription_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setServiceDescription("virtualdesk_description");
		
		Assert.assertEquals("virtualdesk_description", serviceConfiguration.getServiceDescription());
	}
	
	@Test
	public void setMaxNumberOfDeskCreated_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setMaxNumberOfDeskCreated(58);
		
		Assert.assertEquals(58, serviceConfiguration.getMaxNumberOfDeskCreated());
	}
	
	@Test
	public void setQuestionRecievedMessage_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setQuestionReceivedMessage("Test Message");
		
		Assert.assertEquals("Test Message", serviceConfiguration.getQuestionReceivedMessage());
	}
	
	@Test
	public void setNumberOfRecentQuestions_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setNumberOfRecentQuestions(76);
		
		Assert.assertEquals(76, serviceConfiguration.getNumberOfRecentQuestions());
	}
	
	@Test
	public void setHistoryPageSize_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setHistoryPageSize(93);
		
		Assert.assertEquals(93, serviceConfiguration.getHistoryPageSize());
	}
	
	@Test
	public void setHistorySearchEnabled_SetAnyValues_CanGetThatValueBack()
	{
		serviceConfiguration.setHistorySearchEnabled(true);
		
		Assert.assertTrue(serviceConfiguration.isHistorySearchEnabled());
	}
}
