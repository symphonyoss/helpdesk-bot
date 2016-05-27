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

public interface IServiceConfiguration
{
	// Service Information
	int getServiceID();

	void setServiceID(int serviceID);

	String getVirtualDeskSubDomain();

	void setVirtualDeskSubDomain(String virtualDeskSubDomain);

	String getServiceDescription();

	void setServiceDescription(String description);

	// Service Properties Configuration
	int getMaxNumberOfDeskCreated();

	void setMaxNumberOfDeskCreated(int maxNumberOfDeskCreated);
	
	String getQuestionReceivedMessage();
	
	void setQuestionReceivedMessage(String message);
	
	int getNumberOfRecentQuestions();
	
	void setNumberOfRecentQuestions(int numOfQuestions);
	
	int getHistoryPageSize();
	
	void setHistoryPageSize(int pageSize);
	
	boolean isHistorySearchEnabled();
	
	void setHistorySearchEnabled(boolean historySearchEnabled);
}
