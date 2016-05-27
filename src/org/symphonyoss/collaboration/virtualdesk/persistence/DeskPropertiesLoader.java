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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeskPropertiesLoader implements RowCallbackHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(DeskPropertiesLoader.class);
	
	private Map <String, Desk> deskMap;

	public DeskPropertiesLoader(Collection <Desk> deskList)
	{
		deskMap = new HashMap <String, Desk>();

		for (Desk desk : deskList)
		{
			deskMap.put(desk.getName(), desk);
		}
	}

	@Override
	public void processRow(ResultSet resultSet) throws SQLException
	{
		String deskName = resultSet.getString("deskName");

		Desk desk = deskMap.get(deskName);
		if (desk != null)
		{
			try
			{
				String propName = resultSet.getString("name");
				if (propName.equalsIgnoreCase("Creator"))
				{
					desk.setCreator(resultSet.getString("propValue"));
				}
				else if (propName.equalsIgnoreCase("DeskOwners"))
				{
					String value = resultSet.getString("propValue");
	
					desk.syncOwners(extractUserSet(value));
				}
				else if (propName.equalsIgnoreCase("DeskAdmins"))
				{
					String value = resultSet.getString("propValue");
	
					desk.syncAdmins(extractUserSet(value));
				}
				else if (propName.equalsIgnoreCase("DeskMembers"))
				{
					String value = resultSet.getString("propValue");
					
					desk.syncMembers(extractUserSet(value));
				}
				else if (propName.equalsIgnoreCase("DeskParticipants"))
				{
					String value = resultSet.getString("propValue");
	
					desk.syncParticipant(extractUserSet(value));
				}
				else if (propName.equalsIgnoreCase("DeskAlias"))
				{
					desk.setDeskAliasName(resultSet.getString("propValue"));
				}
			}
			catch (Throwable e)
			{
				Logger.warn("Failed to load the desk properties", e);
			}
		}
	}

	private Set <String> extractUserSet(String value)
	{
		Set <String> ownerSet = new HashSet <String>();

		if (value != null && value.length() > 0)
		{
			String[] users = value.split(",");

			if (users.length > 0)
			{
				ownerSet.addAll(Arrays.asList(users));
			}
		}

		return ownerSet;
	}
}
