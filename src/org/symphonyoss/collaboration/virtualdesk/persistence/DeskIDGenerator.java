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

package org.symphonyoss.collaboration.virtualdesk.persistence;

import javax.sql.DataSource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;



public class DeskIDGenerator
{
	private JdbcTemplate jdbcTemplate;

	private int typeID;

	public DeskIDGenerator(int typeID)
	{
		this.typeID = typeID;
	}

	public void setDataSource(DataSource dataSource)
	{
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int getNextDeskID()
	{
		try
		{
			return getNextID();
		}
		catch (IncorrectResultSizeDataAccessException e)
		{
			createNewID();

			return 1;
		}
	}

	private void createNewID()
	{
		final String INSERT_NEW_ID = "INSERT INTO ofID VALUES (?, ?)";

		jdbcTemplate.update(INSERT_NEW_ID, typeID, 1);
		

	}

	private int getNextID()
	{
		final String GET_CURRENT_ID = "SELECT id FROM ofID WHERE idType = ?";

		int currentID = jdbcTemplate.queryForInt(GET_CURRENT_ID, typeID);

		int nextID = currentID + 1;

		final String UPDATE_ID = "UPDATE ofID SET id = ? WHERE idType = ? and id = ?";

		int rowAffected = jdbcTemplate.update(UPDATE_ID, nextID, typeID, currentID);


		
		return rowAffected == 0 ? -1 : nextID;
	}
}
