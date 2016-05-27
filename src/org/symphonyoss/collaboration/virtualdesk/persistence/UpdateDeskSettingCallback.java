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

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;

public class UpdateDeskSettingCallback extends TransactionCallbackWithoutResult
{
	private Desk deskRoom;
	private IServiceConfiguration serviceConfiguration;
	private JdbcTemplate jdbcTemplate; 
	private DeskIDGenerator idGenerator;
	
	public UpdateDeskSettingCallback(Desk deskRoom, IServiceConfiguration serviceConfiguration, JdbcTemplate jdbcTemplate, DeskIDGenerator idGenerator)
	{
		this.deskRoom = deskRoom;
		this.serviceConfiguration = serviceConfiguration;
		this.jdbcTemplate = jdbcTemplate;
		this.idGenerator = idGenerator;
	}
	
	@Override
	protected void doInTransactionWithoutResult(TransactionStatus arg0)
	{
		if (deskRoom.isPersistent())
		{
			if (deskRoom.getID() != -1)
			{
				final String UPDATE_DESK = "UPDATE ofMucRoom SET naturalName = ?, description = ?, membersOnly = ?, modificationDate = ? WHERE serviceID = ? AND roomID = ?";

				String currentTime = DateUtils.dateToMillis(new Date());

				jdbcTemplate.update(UPDATE_DESK, deskRoom.getNaturalName(), deskRoom.getDescription(), deskRoom.isMembersOnly(), currentTime, serviceConfiguration.getServiceID(), deskRoom.getID());

				final String UPDATE_DESKPROP = "UPDATE ofMucRoomProp SET propValue = ? WHERE roomID = ? AND name = ? AND serviceID = ?";

				jdbcTemplate.update(UPDATE_DESKPROP, StringUtils.join(deskRoom.getOwners(), ","), deskRoom.getID(), "DeskOwners", serviceConfiguration.getServiceID());
				jdbcTemplate.update(UPDATE_DESKPROP, StringUtils.join(deskRoom.getAdmins(), ","), deskRoom.getID(), "DeskAdmins", serviceConfiguration.getServiceID());
				jdbcTemplate.update(UPDATE_DESKPROP, StringUtils.join(deskRoom.getMembers(), ","), deskRoom.getID(), "DeskMembers", serviceConfiguration.getServiceID());
				jdbcTemplate.update(UPDATE_DESKPROP, StringUtils.join(deskRoom.getParticipants(), ","), deskRoom.getID(), "DeskParticipants", serviceConfiguration.getServiceID());
				jdbcTemplate.update(UPDATE_DESKPROP, deskRoom.getDeskAliasName(), deskRoom.getID(), "DeskAlias", serviceConfiguration.getServiceID());
				

			}
			else
			{
				final String INSERT_DESK = "INSERT INTO ofMucRoom(serviceID, roomID, creationDate," +
						"modificationDate, name, naturalName, description, lockedDate, canChangeSubject," +
						"maxUsers, publicRoom, moderated, membersOnly, canInvite," +
						"canDiscoverJID, logEnabled, rolesToBroadcast, useReservedNick" +
						",canChangeNick, canRegister) VALUES (?, ?, ?, ?, ?, ?, ?, '000000000000000'," +
						"0, 0, 1, 0, ?, 0, 0, 0, 7, 0, 0, 0)";

				int deskID = idGenerator.getNextDeskID();

				deskRoom.setID(deskID);

				jdbcTemplate.update(INSERT_DESK, serviceConfiguration.getServiceID(), deskID,
						DateUtils.dateToMillis(new Date()), DateUtils.dateToMillis(new Date()),
						deskRoom.getJID().getNode(), deskRoom.getNaturalName(), deskRoom.getDescription(),
						deskRoom.isMembersOnly());

				final String INSERT_DESKPROP = "INSERT INTO ofMucRoomProp VALUES (?, ?, ?, ?)";

				jdbcTemplate.update(INSERT_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID(), "Creator", deskRoom.getCreator());

				jdbcTemplate.update(INSERT_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID(), "DeskOwners", StringUtils.join(deskRoom.getOwners(), ","));
				jdbcTemplate.update(INSERT_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID(), "DeskAdmins", StringUtils.join(deskRoom.getAdmins(), ","));
				jdbcTemplate.update(INSERT_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID(), "DeskMembers", StringUtils.join(deskRoom.getMembers(), ","));
				jdbcTemplate.update(INSERT_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID(), "DeskParticipants", StringUtils.join(deskRoom.getParticipants(), ","));
				jdbcTemplate.update(INSERT_DESKPROP, serviceConfiguration.getServiceID(), deskRoom.getID(), "DeskAlias", deskRoom.getDeskAliasName());
				

			}
		}
		else
		{
			final String DELETE_DESKPROP = "DELETE FROM ofMucRoomProp WHERE roomID = ? AND serviceID = ?";

			jdbcTemplate.update(DELETE_DESKPROP, deskRoom.getID(), serviceConfiguration.getServiceID());

			final String DELETE_DESK = "DELETE FROM ofMucRoom WHERE serviceID = ? AND roomID = ?";

			jdbcTemplate.update(DELETE_DESK, serviceConfiguration.getServiceID(), deskRoom.getID());
			
			final String DELETE_DESK_QUESTIONS = "DELETE FROM vdDeskQuestion WHERE serviceID = ? AND roomID = ?";

			jdbcTemplate.update(DELETE_DESK_QUESTIONS, serviceConfiguration.getServiceID(), deskRoom.getID());

			deskRoom.setID(-1);
			

		}
	}

}
