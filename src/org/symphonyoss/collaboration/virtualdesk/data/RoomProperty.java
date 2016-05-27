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

public enum RoomProperty
{

	RoomName ("muc#roomconfig_roomname", "Desk Name", "text-single"),
	Description ("muc#roomconfig_roomdesc", "Description", "text-single"),
	ChangeSubject ("muc#roomconfig_changesubject", "Allow Occupants to Change Subject", "boolean"),
	RoomAdmins ("muc#roomconfig_roomadmins", "Desk Admins", "jid-multi"),
	RoomOwners ("muc#roomconfig_roomowners", "Desk Owners", "jid-multi"),
	RoomParticipants ("muc#roomconfig_roomparticipants", "Desk Participants", "jid-multi"),
	RoomMembers ("muc#roomconfig_roommembers", "Desk Members", "jid-multi"),
	PersistentRoom ("muc#roomconfig_persistentroom", "Desk is Persistent", "boolean"),
	MembersOnly ("muc#roomconfig_membersonly", "Desk is Members-only", "boolean"),
	DeskAlias ("muc#roomconfig_deskalias", "Desk Alias Name", "text-single");

	RoomProperty(String id, String label, String type)
	{
		this.id = id;
		this.label = label;
		this.type = type;
	}

	private final String id;
	private final String label;
	private final String type;

	public String getId()
	{
		return id;
	}

	public String getLabel()
	{
		return label;
	}

	public String getType()
	{
		return type;
	}
}
