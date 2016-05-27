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

package org.symphonyoss.collaboration.virtualdesk.persistence.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;

public class DeskMapper implements RowMapper <Desk>
{
	private String virtualDeskDomain;

	public DeskMapper(String virtualDeskDomain)
	{
		this.virtualDeskDomain = virtualDeskDomain;
	}

	@Override
	public Desk mapRow(ResultSet resultSet, int arg1) throws SQLException
	{
		String deskName = resultSet.getString("name");

		Desk desk = new Desk(deskName, virtualDeskDomain);

		desk.setID(resultSet.getInt("roomID"));
		desk.setNaturalName(resultSet.getString("naturalName"));
		desk.setSubject(resultSet.getString("subject"));
		desk.setDescription(resultSet.getString("description"));
		desk.init();

		return desk;
	}
}
