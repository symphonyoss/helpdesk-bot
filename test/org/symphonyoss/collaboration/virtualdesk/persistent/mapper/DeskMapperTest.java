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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.ResultSet;
import java.sql.SQLException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.persistence.mapper.DeskMapper;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;

public class DeskMapperTest
{
	private DeskMapper mapper;

	@Before
	public void before()
	{
		mapper = new DeskMapper(TestConst.VIRTUALDESK_DOMAIN);
	}

	@Test
	public void mapRow_AnyConditions_ReturnDesk() throws SQLException
	{
		ResultSet mockResultSet = mock(ResultSet.class);

		when(mockResultSet.getString("name")).thenReturn("Desk1");
		when(mockResultSet.getString("naturalName")).thenReturn("Desk1_natural");
		when(mockResultSet.getInt("roomID")).thenReturn(2);
		when(mockResultSet.getString("subject")).thenReturn("Desk subject");
		when(mockResultSet.getString("description")).thenReturn("Desk description");

		Desk desk = mapper.mapRow(mockResultSet, 0);

		Assert.assertEquals("Desk1", desk.getName());
		Assert.assertEquals("Desk1_natural", desk.getNaturalName());
		Assert.assertEquals(2, desk.getID());
		Assert.assertEquals("Desk subject", desk.getSubject());
		Assert.assertEquals("Desk description", desk.getDescription());
	}
}
