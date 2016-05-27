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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPersistenceManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence.Show;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;

public class Desk
{
	private int deskID;

	private String deskName;
	private String subject;
	private String description;

	protected String naturalName;
	
	private String creator; // Store bare JID of creator

	private boolean canDiscoverJID;
	private boolean isNewlyCreated;

	private JID deskJID;

	protected User virtualDeskUser;

	protected Set <String> deskOwners; // Store bare JID of users
	protected Set <String> deskAdmins;
	protected Set <String> deskMembers;
	protected Set <String> deskParticipants;

	protected OccupantMap memberMap;
	protected OccupantMap participantMap;

	protected Map <String, UserState> userState;

	protected boolean isPersistent;
	protected boolean isMembersOnly;
	
	protected DeskPersistenceManager deskPersistenceManager;

	public Desk(String name, String domain)
	{
		this.deskName = name;

		deskJID = new JID(name, domain, null);

		naturalName = JID.unescapeNode(name);
		
		deskOwners = new LinkedHashSet <String>();
		deskAdmins = new LinkedHashSet <String>();
		deskMembers = new LinkedHashSet <String>();
		deskParticipants = new LinkedHashSet <String>();

		userState = new LinkedHashMap <String, UserState>();

		memberMap = new OccupantMap();
		participantMap = new OccupantMap();

		virtualDeskUser = new User(deskJID, deskName, Affiliation.admin, Role.moderator);

		isNewlyCreated = true;
		
		isPersistent = true;

		subject = "";
		description = "";
	}

	public int getID()
	{
		return deskID;
	}

	public void setID(int deskID)
	{
		this.deskID = deskID;
	}

	public String getName()
	{
		return deskName;
	}

	public void setName(String deskName)
	{
		this.deskName =  deskName;
	}

	public void setDeskAliasName(String aliasName)
	{
		virtualDeskUser = new User(deskJID, aliasName, Affiliation.admin, Role.moderator);
	}
	
	public String getDeskAliasName()
	{
		return virtualDeskUser.getNickname();
	}
	
	public JID getJID()
	{
		return deskJID;
	}

	public String getNaturalName()
	{
		return naturalName;
	}
	
	public void setNaturalName(String naturalName)
	{
		this.naturalName = JID.unescapeNode(naturalName);
	}
	
	public String getCreator()
	{
		return creator;
	}

	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean canDiscoverJID()
	{
		return canDiscoverJID;
	}

	public void setCanDiscoverJID(boolean canDiscoverJID)
	{
		this.canDiscoverJID = canDiscoverJID;
	}

	public User getVirtualDeskUser()
	{
		return virtualDeskUser;
	}
	
	public Set <String> getMembers()
	{
		return deskMembers;
	}

	public Set <String> getParticipants()
	{
		return deskParticipants;
	}

	public Set <String> getAdmins()
	{
		return deskAdmins;
	}

	public Set <String> getOwners()
	{
		return deskOwners;
	}

	public boolean isNewlyCreated()
	{
		return isNewlyCreated;
	}

	public boolean isAdmin(String userBareJID)
	{
		return deskAdmins.contains(userBareJID);
	}

	public boolean isOwner(String userBareJID)
	{
		return deskOwners.contains(userBareJID);
	}

	public boolean isMember(String userBareJID)
	{
		return deskMembers.contains(userBareJID);
	}

	public boolean hasDeskMemberPrivilege(String userBareJID)
	{
		return deskAdmins.contains(userBareJID) || deskOwners.contains(userBareJID) || deskMembers.contains(userBareJID);
	}
	
	public boolean isMemberParticipant(String userBareJID)
	{
		return deskParticipants.contains(userBareJID);
	}

	public boolean isPersistent()
	{
		return isPersistent;
	}

	public boolean isMembersOnly()
	{
		return isMembersOnly;
	}

	public void isMembersOnly(boolean isMembersOnly)
	{
		this.isMembersOnly = isMembersOnly;
	}

	public void init()
	{
		isNewlyCreated = false;
	}

	public void addOwner(String ownerBareJID)
	{
		deskOwners.add(ownerBareJID);
	}

	public void addAdmin(String adminBareJID)
	{
		deskAdmins.add(adminBareJID);
	}
	
	public void addMember(String participantBareJID)
	{
		deskMembers.add(participantBareJID);
	}

	public void addParticipants(String participantBareJID)
	{
		deskParticipants.add(participantBareJID);
	}

	public void syncOwners(Set <String> owners)
	{
		deskOwners = owners;
	}

	public void syncAdmins(Set <String> admins)
	{
		deskAdmins = admins;
	}
	
	public void syncMembers(Set <String> members)
	{
		deskMembers = members;
	}

	public void syncParticipant(Set <String> participants)
	{
		deskParticipants = participants;
	}

	public Collection <User> getCurrentMembers()
	{
		return memberMap.getOccupants();
	}

	public int getCurrentMemberCount()
	{
		return memberMap.getOccupantCount();
	}

	public Collection <User> getCurrentParticipants()
	{
		return participantMap.getOccupants();
	}

	public int getCurrentParticipantCount()
	{
		return participantMap.getOccupantCount();
	}

	public void setDeskPersistenceManager(DeskPersistenceManager deskPersistenceManager)
	{
		this.deskPersistenceManager = deskPersistenceManager;
	}
	
	public void addOccupant(User user)
	{
		if (user.isDeskMember())
		{
			memberMap.addOccupant(user.getJID().toBareJID(), user.getNickname(), user);
		}
		else
		{
			participantMap.addOccupant(user.getJID().toBareJID(), user.getNickname(), user);
		}
	}

	public void removeOccupant(User user)
	{
		if (user.isDeskMember())
		{
			memberMap.removeOccupantByJID(user.getJID().toBareJID());
		}
		else
		{
			participantMap.removeOccupantByJID(user.getJID().toBareJID());
		}
	}

	public User getOccupantByJID(JID userJID)
	{
		String bareJID = userJID.toBareJID();

		User user = memberMap.getUserByJID(bareJID);
		if (user != null)
		{
			return user;
		}

		return participantMap.getUserByJID(bareJID);
	}

	public User getOccupantByNickname(String nickname)
	{
		User user = memberMap.getUserByNickname(nickname);
		if (user != null)
		{
			return user;
		}

		return participantMap.getUserByNickname(nickname);
	}
	
	public boolean isNicknameExisted(String nickname)
	{
		return virtualDeskUser.getNickname().equalsIgnoreCase(nickname) || getOccupantByNickname(nickname) != null;
	}

	public PresenceType getDeskPresence()
	{
		if (memberMap.getOccupantCount() == 0)
		{
			return PresenceType.ExtendedAway;
		}

		for (User member : memberMap.getOccupants())
		{
			Show memberShow = member.getPresence().getShow();
			if (memberShow == null || memberShow == Show.chat)
			{
				return PresenceType.Online;
			}
		}

		return PresenceType.Away;
	}

	public Collection<UserState> getAllQuestions()
	{
		return userState.values();
	}
	
	public void loadQuestions(List<UserState> questionList)
	{
		userState.clear();
		
		for (UserState question : questionList)
		{
			String nicknameKey = question.getPosterNickname().toLowerCase();
			
			userState.put(nicknameKey, question);
		}
	}
	
	public UserState getQuestion(String userNickname)
	{
		String nicknameKey = userNickname.toLowerCase();
		
		if (!userState.containsKey(nicknameKey))
		{
			return null;
		}

		return userState.get(nicknameKey);
	}
	
	public void resetQuestionState(String userNickname)
	{
		String nicknameKey = userNickname.toLowerCase();
		
		UserState question = userState.get(nicknameKey);
		
		if (question != null)
		{
			question.setState(WorkflowState.AwaitResponse);
		}
	}
	
	public void updateQuestion(String userNickname)
	{
		UserState question = userState.get(userNickname);
		
		if (question != null)
		{
			deskPersistenceManager.updateDeskQuestion(deskID, question);
		}
	}
	
	public void closeQuestion(String userNickname)
	{
		deskPersistenceManager.deleteDeskQuestion(deskID, userNickname);
		
		String nicknameKey = userNickname.toLowerCase();
		
		userState.remove(nicknameKey);
	}

	public void addNewQuestion(String posterNickname, String posterJID, String question)
	{
		UserState state = new UserState(posterNickname, posterJID, question);

		String nicknameKey = posterNickname.toLowerCase();
		
		userState.put(nicknameKey, state);
		
		deskPersistenceManager.saveDeskQuestion(deskID, state);
	}
}
