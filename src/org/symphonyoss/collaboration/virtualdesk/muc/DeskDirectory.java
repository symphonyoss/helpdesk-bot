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
import org.springframework.dao.DataAccessException;

import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPersistenceManager;

public class DeskDirectory implements IDeskDirectory
{
	private DeskPersistenceManager deskPersistenceManager;

	private String virtualDeskDomain;

	private Map <String, Desk> deskMap; // Mapping between desk name and desk object
	
	private Map <String, Conversation> conversationMap; // Mapping between conference desk name and desk object

	private Map <String, Integer> deskCountMap;

	public DeskDirectory(DeskPersistenceManager deskPersistenceManager)
	{
		this.deskPersistenceManager = deskPersistenceManager;

		deskMap = new HashMap <String, Desk>();
		
		conversationMap = new HashMap <String, Conversation>();

		deskCountMap = new HashMap <String, Integer>();
	}

	public DeskDirectory(String virtualDeskDomain, DeskPersistenceManager deskPersistenceManager)
	{
		this.virtualDeskDomain = virtualDeskDomain;
		this.deskPersistenceManager = deskPersistenceManager;

		deskMap = new HashMap <String, Desk>();
	}

	public void loadDesks()
	{
		Collection <Desk> deskCollection = deskPersistenceManager.getDesks(virtualDeskDomain);

		deskMap.clear();

		deskCountMap.clear();

		for (Desk desk : deskCollection)
		{
			// Assign desk persistent manager to desk in order to save question to database
			desk.setDeskPersistenceManager(deskPersistenceManager);
			
			deskMap.put(desk.getName(), desk);

			increaseDeskCount(desk.getCreator());
		}
		

	}

	public void updateDeskSetting(Desk deskRoom)
	{
		deskPersistenceManager.updateDeskSetting(deskRoom);
	}

	public boolean contains(String deskName)
	{
		return deskMap.containsKey(deskName);
	}

	public boolean containsConversation(String coversationName)
	{
		return conversationMap.containsKey(coversationName);
	}

	public Desk getDesk(String deskName)
	{
		return deskMap.get(deskName);
	}
	
	public Conversation getConversation(String deskName)
	{
		return conversationMap.get(deskName);
	}

	public Desk createDesk(String deskName, String creatorBareJID)
	{
		Desk desk = deskMap.get(deskName);

		if (desk == null)
		{
			desk = new Desk(deskName, virtualDeskDomain);
			desk.setCanDiscoverJID(false);
			desk.setID(-1);
			desk.setCreator(creatorBareJID);
			desk.setDeskPersistenceManager(deskPersistenceManager);

			deskMap.put(deskName, desk);

			increaseDeskCount(creatorBareJID);
		}

		return desk;
	}
	
	@Override
	public Conversation createDeskConversation(String deskName, String creatorBareJID, Desk parent)
	{
		Conversation conversation = conversationMap.get(deskName);

		if (conversation == null)
		{
			conversation = new Conversation(deskName, virtualDeskDomain, parent);
			conversation.setCanDiscoverJID(false);
			conversation.setID(-1);

			conversationMap.put(deskName, conversation);
			

		}
		
		return conversation;
	}

	private void increaseDeskCount(String creatorBareJID)
	{
		Integer count = deskCountMap.get(creatorBareJID);

		if (count == null)
		{
			count = 0;
		}

		deskCountMap.put(creatorBareJID, ++count);
	}
	
	private void decreaseDeskCount(String creatorBareJID)
	{
		Integer count = deskCountMap.get(creatorBareJID);

		if (count == null || count.intValue() <= 0)
		{
			return;
		}

		deskCountMap.put(creatorBareJID, --count);
	}

	@Override
	public Collection <Desk> getAllDesks()
	{
		return deskMap.values();
	}
	
	@Override
	public Collection <Conversation> getAllConversation()
	{
		return conversationMap.values();
	}

	public void setDomain(String domain)
	{
		virtualDeskDomain = domain;
	}

	@Override
	public int getCurrentNumberOfDeskCreated(String userBareJID)
	{
		Integer count = deskCountMap.get(userBareJID);

		if (count == null)
		{
			return 0;
		}

		return count;
	}

	@Override
	public void destroyDesk(Desk deskRoom)
	{
		try
		{
			deskPersistenceManager.destroyDesk(deskRoom);

			decreaseDeskCount(deskRoom.getCreator());
			
			deskMap.remove(deskRoom.getName());
		}
		catch (DataAccessException e)
		{

		}
	}

	@Override
	public void removeDeskConversation(String deskName)
	{
		if( conversationMap.containsKey(deskName))
		{
			conversationMap.remove(deskName);
		

		}
	}
}
