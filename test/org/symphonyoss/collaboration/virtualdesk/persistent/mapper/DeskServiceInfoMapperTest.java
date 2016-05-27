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

package org.symphonyoss.collaboration.virtualdesk.persistent.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.DeskServiceInfo;
import org.symphonyoss.collaboration.virtualdesk.persistence.mapper.DeskServiceInfoMapper;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DeskServiceInfoMapperTest
{
	private DeskServiceInfoMapper serviceInfoMapper;
	private @Mocked @NonStrict ResultSet resultSet;
	
	@Before
	public void before()
	{
		serviceInfoMapper = new DeskServiceInfoMapper();
	}
	
	@Test
	public void mapRow_AnyConditions_ReturnDeskInfo() throws SQLException
	{
		new Expectations()
		{
			{
				resultSet.getInt("serviceID"); result = 5;
				resultSet.getString("subdomain"); result = "virtualdesk";
				resultSet.getString("description"); result = "description";
			}
		};
		
		DeskServiceInfo serviceInfo = serviceInfoMapper.mapRow(resultSet, 1);
		
		Assert.assertEquals(5, serviceInfo.getServiceID());
		Assert.assertEquals("virtualdesk", serviceInfo.getSubDomain());
		Assert.assertEquals("description", serviceInfo.getDescription());
		Assert.assertTrue(serviceInfo.isPersistentService());
	}
}
