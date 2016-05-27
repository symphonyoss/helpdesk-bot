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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;


public class DestroyDeskCallback extends TransactionCallbackWithoutResult
{
	private Desk deskRoom;
	private IServiceConfiguration serviceConfiguration;
	private JdbcTemplate jdbcTemplate;

	public DestroyDeskCallback(Desk deskRoom, IServiceConfiguration serviceConfiguration, JdbcTemplate jdbcTemplate)
	{
		this.deskRoom = deskRoom;
		this.serviceConfiguration = serviceConfiguration;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	protected void doInTransactionWithoutResult(TransactionStatus arg0)
	{
		final String DELETE_DESK_QUESTIONS = "DELETE FROM vdDeskQuestion WHERE serviceID = ? AND roomID = ?";

		jdbcTemplate.update(DELETE_DESK_QUESTIONS, serviceConfiguration.getServiceID(), deskRoom.getID());

		final String DELETE_DESKPROP = "DELETE FROM ofMucRoomProp WHERE serviceID = ? AND roomID = ?";

		jdbcTemplate.update(DELETE_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID());

		final String DELETE_DESK = "DELETE FROM ofMucRoom WHERE serviceID = ? AND roomID = ?";

		jdbcTemplate.update(DELETE_DESK, serviceConfiguration.getServiceID(), deskRoom.getID());

		deskRoom.setID(-1);
		

	}

}
