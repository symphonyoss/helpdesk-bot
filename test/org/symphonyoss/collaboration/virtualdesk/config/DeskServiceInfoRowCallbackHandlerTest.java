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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeskServiceInfoRowCallbackHandlerTest
{
	private DeskServiceInfoRowCallbackHandler rowHandler;

	private IServiceConfiguration serviceConfiguration;

	private ResultSet mockResultSet;

	@Before
	public void before()
	{
		DataSource mockDataSource = mock(DataSource.class);

		serviceConfiguration = new ServiceConfiguration("virtualdesk", mockDataSource);

		mockResultSet = mock(ResultSet.class);

		rowHandler = new DeskServiceInfoRowCallbackHandler(serviceConfiguration);
	}

	@Test
	public void processRow_AnyConditions_SetServiceInfoToServiceConfiguration() throws SQLException
	{
		when(mockResultSet.getInt("serviceID")).thenReturn(4);
		when(mockResultSet.getString("subdomain")).thenReturn("virtualdeskdomain");
		when(mockResultSet.getString("description")).thenReturn("service description");

		rowHandler.processRow(mockResultSet);

		Assert.assertEquals(4, serviceConfiguration.getServiceID());
		Assert.assertEquals("virtualdeskdomain", serviceConfiguration.getVirtualDeskSubDomain());
		Assert.assertEquals("service description", serviceConfiguration.getServiceDescription());
	}
}
