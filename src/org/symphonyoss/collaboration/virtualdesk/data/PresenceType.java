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

import org.xmpp.packet.Presence.Show;

public enum PresenceType
{
	Online ("Online", 1, null),
	FreeToChat("Free to chat", 1, Show.chat),
	Away("Away", 0, Show.away),
	ExtendedAway("Extended away", 0, Show.xa),
	OnThePhone("On the phone", 0, Show.away),
	DoNotDisturb("Do not disturb", 0, Show.dnd);

	private String status;
	private int priority;
	private Show show;

	PresenceType(String status, int priority, Show show)
	{
		this.status = status;
		this.priority = priority;
		this.show = show;
	}

	public String getStatus()
	{
		return status;
	}

	public int getPriority()
	{
		return priority;
	}

	public Show getShow()
	{
		return show;
	}

	public static PresenceType getPresence(String statusName)
	{
		for(PresenceType type : PresenceType.values())
		{
			if(type.status.equalsIgnoreCase(statusName))
			{
				return type;
			}
		}
		
		return null;
	}

}
