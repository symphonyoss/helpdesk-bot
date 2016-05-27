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

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class ServiceConfiguration implements IServiceConfiguration
{
	public static final int NO_SERVICEID = -1;
	
	public static final int DEFAULT_SERVICEID = 50;

	public static final String MAXNUMBER_OF_DESKCREATED = "MaxNumOfDeskCreated";
	public static final String QUESTION_RECEIVED_MESSAGE = "QuestionReceivedMessage";
	public static final String NUMBER_OF_RECENTQUESTIONS = "NumberOfRecentQuestion";
	public static final String HISTORY_PAGESIZE = "HistoryPageSize";

	public static final int DEFAULT_MAXNUMBER_OF_DESKCREATED = 20;
	public static final int DEFAULT_NUMBER_OF_RECENTQUESTIONS = 20;
	public static final String DEFAULT_QUESTION_RECEIVED_MESSAGE = "Your query has been received and will be answered shortly.";
	public static final int DEFAULT_HISTORY_PAGESIZE = 20;

	@SuppressWarnings ("unused")
	private String subDomain;
	
	private JdbcTemplate jdbcTemplate;

	// Desk Service Information
	private int serviceID;
	private String virtualDeskSubDomain;
	private String serviceDescription;

	// Desk Service Properties
	private int maxNumberOfDeskCreated;
	private int numberOfRecentQuestions;
	private String questionReceviedMessage;
	private int historyPageSize;
	private boolean historySearchEnabled;

	public ServiceConfiguration(String subDomain, DataSource dataSource)
	{
		this.subDomain = subDomain;

		jdbcTemplate = new JdbcTemplate(dataSource);

		// Initialize fields
		serviceID = DEFAULT_SERVICEID;

		maxNumberOfDeskCreated = DEFAULT_MAXNUMBER_OF_DESKCREATED;
		numberOfRecentQuestions = DEFAULT_NUMBER_OF_RECENTQUESTIONS;
		questionReceviedMessage = DEFAULT_QUESTION_RECEIVED_MESSAGE;
		historyPageSize = DEFAULT_HISTORY_PAGESIZE;
		
		historySearchEnabled = false;
	}

	public void init()
	{
		loadDeskServiceInfo();

		loadDeskServiceProperties();
	}

	private void loadDeskServiceInfo()
	{
		virtualDeskSubDomain = "virtualdesk";
		serviceDescription = "Virtual Desk Service";
	}

	private void loadDeskServiceProperties()
	{
		if (serviceID == NO_SERVICEID)
		{
			return;
		}

		final String GET_DESKPROP = "SELECT name, propValue FROM ofMucServiceProp WHERE serviceID = ?";

		jdbcTemplate.query(GET_DESKPROP, new Object[]{serviceID}, new ServicePropertyRowCallbackHandler(this));
	}

	public int getServiceID()
	{
		return serviceID;
	}

	public String getVirtualDeskSubDomain()
	{
		return virtualDeskSubDomain;
	}

	public String getServiceDescription()
	{
		return serviceDescription;
	}

	public int getMaxNumberOfDeskCreated()
	{
		return maxNumberOfDeskCreated;
	}

	@Override
	public void setServiceID(int serviceID)
	{
		this.serviceID = serviceID;
	}

	@Override
	public void setVirtualDeskSubDomain(String virtualDeskSubDomain)
	{
		this.virtualDeskSubDomain = virtualDeskSubDomain;
	}

	@Override
	public void setServiceDescription(String description)
	{
		this.serviceDescription = description;
	}

	@Override
	public void setMaxNumberOfDeskCreated(int maxNumberOfDeskCreated)
	{
		this.maxNumberOfDeskCreated = maxNumberOfDeskCreated;
	}

	@Override
	public String getQuestionReceivedMessage()
	{
		return questionReceviedMessage;
	}

	@Override
	public void setQuestionReceivedMessage(String message)
	{
		this.questionReceviedMessage = message;
	}

	@Override
	public int getNumberOfRecentQuestions()
	{
		return numberOfRecentQuestions;
	}

	@Override
	public void setNumberOfRecentQuestions(int numOfQuestions) 
	{
		this.numberOfRecentQuestions = numOfQuestions;
	}

	@Override
	public int getHistoryPageSize()
	{
		return historyPageSize;
	}

	@Override
	public void setHistoryPageSize(int pageSize)
	{
		historyPageSize = pageSize;
	}

	public boolean isHistorySearchEnabled()
	{
		return historySearchEnabled;
	}

	public void setHistorySearchEnabled(boolean historySearchEnabled)
	{
		this.historySearchEnabled = historySearchEnabled;
	}
}
