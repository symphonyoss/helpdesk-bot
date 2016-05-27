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

import java.util.ArrayList;
import java.util.Collection;
import javax.sql.DataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;

public class DeskNonPersistenceManager extends DeskPersistenceManager
{
	public DeskNonPersistenceManager(IServiceConfiguration serviceConfiguration)
	{
		super(serviceConfiguration);
	}
	
	@Override
	public void setDeskIDGenerator(DeskIDGenerator idGenerator)
	{
	}

	@Override
	public void setDataSource(DataSource dataSource)
	{
	}

	@Override
	public void setTransactionManager(PlatformTransactionManager txManager)
	{
	}

	@Override
	public Collection <Desk> getDesks(String virtualDeskDomain)
	{
		return new ArrayList<Desk>();
	}

	@Override
	public void updateDeskSetting(final Desk deskRoom)
	{
	}
	
	@Override
	public void destroyDesk(final Desk deskRoom)
	{
	}
	
	@Override
	public void saveDeskQuestion(int deskID, UserState question)
	{
	}
	
	@Override
	public void updateDeskQuestion(int deskID, UserState question)
	{
	}
	
	@Override
	public void deleteDeskQuestion(int deskID, String posterNickname)
	{
	}
}
