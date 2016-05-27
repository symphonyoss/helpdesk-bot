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

package org.symphonyoss.collaboration.virtualdesk.muc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.symphonyoss.collaboration.virtualdesk.data.User;

public class OccupantMap
{
	private Map <String, User> userJIDMap;
	private Map <String, User> userNicknameMap; // Required to store lower-case of nickname as a key because nickname is case-insensitive

	public OccupantMap()
	{
		userJIDMap = new HashMap <String, User>();
		userNicknameMap = new HashMap <String, User>();
	}

	public void addOccupant(String userBareJID, String userNickname, User user)
	{
		userJIDMap.put(userBareJID, user);
		userNicknameMap.put(userNickname.toLowerCase(), user);
	}

	public User getUserByJID(String userBareJID)
	{
		return userJIDMap.get(userBareJID);
	}

	public User getUserByNickname(String userNickname)
	{
		return userNicknameMap.get(userNickname.toLowerCase());
	}

	public Collection <User> getOccupants()
	{
		return userJIDMap.values();
	}

	public int getOccupantCount()
	{
		return userJIDMap.size();
	}

	public void removeOccupantByJID(String userBareJID)
	{
		User user = userJIDMap.remove(userBareJID);
		if (user != null)
		{
			String nickname = user.getNickname().toLowerCase();

			userNicknameMap.remove(nickname);
		}
	}
}
