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

package org.symphonyoss.collaboration.virtualdesk.data;

public class DeskServiceInfo
{
	private int serviceID;
	private String subDomain;
	private String description;

	private boolean isPersistentService;

	public DeskServiceInfo(int serviceID, String subDomain, String description, boolean isPersistentService)
	{
		this.serviceID = serviceID;
		this.subDomain = subDomain;
		this.description = description;
		this.isPersistentService = isPersistentService;
	}

	public int getServiceID()
	{
		return serviceID;
	}

	public String getSubDomain()
	{
		return subDomain;
	}

	public String getDescription()
	{
		return description;
	}

	public boolean isPersistentService()
	{
		return isPersistentService;
	}
}
