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

import org.xmpp.packet.JID;

public class User
{
	private JID jid;
	private String nickname;

	private Affiliation affiliation;
	private Role role;

	private PresenceType presence;

	public User(JID jid, String nickname)
	{
		this.jid = jid;
		this.nickname = nickname;

		setPresence(PresenceType.Online);
	}

	public User(JID jid, String nickname, Affiliation affiliation, Role role)
	{
		this(jid, nickname);

		this.affiliation = affiliation;
		this.role = role;
	}

	public boolean isDeskMember()
	{
		return (affiliation == Affiliation.admin) || (affiliation == Affiliation.owner) || (affiliation == Affiliation.member);
	}

	public boolean isDeskAdmin()
	{
		return (affiliation == Affiliation.admin) || (affiliation == Affiliation.owner);
	}
	
	public JID getJID()
	{
		return jid;
	}

	public void setJid(JID jid, String nickname)
	{
		this.jid = jid;
		this.nickname = nickname;
	}

	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public Role getRole()
	{
		return role;
	}

	public void setRole(Role role)
	{
		this.role = role;
	}

	public Affiliation getAffiliation()
	{
		return affiliation;
	}

	public void setAffiliation(Affiliation affiliation)
	{
		this.affiliation = affiliation;
	}

	public PresenceType getPresence()
	{
		return presence;
	}

	public void setPresence(PresenceType presence)
	{
		this.presence = presence;
	}
}
