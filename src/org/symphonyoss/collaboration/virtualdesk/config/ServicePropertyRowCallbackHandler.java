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
import org.springframework.jdbc.core.RowCallbackHandler;

public class ServicePropertyRowCallbackHandler implements RowCallbackHandler
{
	private IServiceConfiguration serviceConfiguration;

	public ServicePropertyRowCallbackHandler(IServiceConfiguration serviceConfiguration)
	{
		this.serviceConfiguration = serviceConfiguration;
	}

	@Override
	public void processRow(ResultSet resultSet) throws SQLException
	{
		String propName = resultSet.getString("name");
		String propValue = resultSet.getString("propValue");

		if (propName.equalsIgnoreCase(ServiceConfiguration.MAXNUMBER_OF_DESKCREATED))
		{
			serviceConfiguration.setMaxNumberOfDeskCreated(Integer.parseInt(propValue));
		}
		else if (propName.equalsIgnoreCase(ServiceConfiguration.QUESTION_RECEIVED_MESSAGE))
		{
			serviceConfiguration.setQuestionReceivedMessage(propValue);
		}
		else if (propName.equalsIgnoreCase(ServiceConfiguration.NUMBER_OF_RECENTQUESTIONS))
		{
			serviceConfiguration.setNumberOfRecentQuestions(Integer.parseInt(propValue));
		}
		else if (propName.equalsIgnoreCase(ServiceConfiguration.HISTORY_PAGESIZE))
		{
			serviceConfiguration.setHistoryPageSize(Integer.parseInt(propValue));
		}
	}

}
