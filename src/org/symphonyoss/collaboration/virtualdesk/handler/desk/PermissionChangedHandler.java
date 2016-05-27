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

package org.symphonyoss.collaboration.virtualdesk.handler.desk;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;

public class PermissionChangedHandler extends AbstractActionHandler
{
	private Desk deskRoom;

	public PermissionChangedHandler(Desk deskRoom)
	{
		this.deskRoom = deskRoom;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", deskRoom.getJID());
		
		List <User> memberList = new ArrayList <User>(deskRoom.getCurrentMembers());

		for (User user : memberList)
		{
			Affiliation newAffiliation = getUserAffiliation(user.getJID().toBareJID());

			// increase user privilege
			if (user.getAffiliation().getValue() < newAffiliation.getValue())
			{
				setAffiliationAndRole(user, newAffiliation);
			}
			// Reduce user privilege
			else if (user.getAffiliation().getValue() > newAffiliation.getValue())
			{
				/**
				 * Incase update Owner->Admin
				 * TODO: Do nothing but update user Role
				 */
				if (newAffiliation == Affiliation.none)
				{
					// Desk member is revoked privilege to participant, so the user has to remove from
					// member list
					deskRoom.removeOccupant(user);
				}

				// Revoke privilege
				setAffiliationAndRole(user, newAffiliation);

				if (newAffiliation == Affiliation.none)
				{
					if (deskRoom.isMembersOnly())
					{
						if (!deskRoom.isMemberParticipant(user.getJID().toBareJID()))
						{
							// kick user from desk because he has no permission to be in desk anymore
							packetList.add(PresenceResponse.createKickUser(user,
									deskRoom.getJID(),
									"You have no permission to join desk."));

							packetList.addAll(PresenceResponse.createLeaveResponse(user, deskRoom.getCurrentMembers(), deskRoom.getJID()));
							
							continue;
						}
					}
					
					deskRoom.addOccupant(user);

					// Send permission change to the desk member
					packetList.addAll(PresenceResponse.createPermissionChange(user,
							deskRoom.getCurrentMembers(),
							deskRoom.getJID(), null));
					
					packetList.add(PresenceResponse.createPermissionChange(user,
							user,
							deskRoom.getJID(),
							null));

					packetList.add(PresenceResponse.createPermissionChange(user,
							user,
							deskRoom.getJID(),
							"Permision is changed by desk owner."));

					//force all user leave room
					for (User deskMember : deskRoom.getCurrentMembers())
					{
						packetList.add(PresenceResponse.createLeaveResponse(deskMember, user, deskRoom.getJID()));
					}

					for (User deskParticipant : deskRoom.getCurrentParticipants())
					{
						if (!deskParticipant.getNickname().equalsIgnoreCase(user.getNickname()))
						{
							packetList.add(PresenceResponse.createLeaveResponse(deskParticipant, user, deskRoom.getJID()));
						}
					}

					//Participant can see only vitual user of the desk
					packetList.add(PresenceResponse.createJoinResponse(deskRoom.getVirtualDeskUser(), user, deskRoom.getJID(), false));
				}
			}
		}

		List <User> participantList = new ArrayList <User>(deskRoom.getCurrentParticipants());

		for (User user : participantList)
		{
			Affiliation newAffiliation = getUserAffiliation(user.getJID().toBareJID());

			if (user.getAffiliation().getValue() >= newAffiliation.getValue())
			{
				// User has no change the permission but has to check that desk is change to member only
				// in case that member list is updated
				if (deskRoom.isMembersOnly())
				{
					if (!deskRoom.isMemberParticipant(user.getJID().toBareJID()))
					{
						deskRoom.removeOccupant(user);

						packetList.add(PresenceResponse.createKickUser(user,
								deskRoom.getJID(),
								"You have no permission to join desk."));

						packetList.addAll(PresenceResponse.createLeaveResponse(user, deskRoom.getCurrentMembers(), deskRoom.getJID()));
					}
				}
			}
			else
			{
				// User is granted to be more privilege, so move from participant list to member list
				deskRoom.removeOccupant(user);

				setAffiliationAndRole(user, newAffiliation);

				deskRoom.addOccupant(user);

				// Notify member and user itself to new privilege
				packetList.addAll(PresenceResponse.createPermissionChange(user,
						deskRoom.getCurrentMembers(),
						deskRoom.getJID(), null));

				packetList.add(PresenceResponse.createPermissionChange(user,
						user,
						deskRoom.getJID(),
						"Permission is changed by desk owner."));

				packetList.add(PresenceResponse.createLeaveResponse(deskRoom.getVirtualDeskUser(),
						user,
						deskRoom.getJID()));

				packetList.addAll(PresenceResponse.createJoinResponse(deskRoom.getCurrentMembers(), user, deskRoom.getJID(), false));

				packetList.addAll(PresenceResponse.createJoinResponse(deskRoom.getCurrentParticipants(), user, deskRoom.getJID(), false));
			}
		}
	}

	private void setAffiliationAndRole(User user, Affiliation newAffiliation)
	{
		Role role = (newAffiliation == Affiliation.admin || newAffiliation == Affiliation.owner || 
					 newAffiliation == Affiliation.member) ? Role.moderator : Role.participant;

		user.setAffiliation(newAffiliation);
		user.setRole(role);
	}

	private Affiliation getUserAffiliation(String userBareJID)
	{
		return deskRoom.isOwner(userBareJID) ? Affiliation.owner :
				deskRoom.isAdmin(userBareJID) ? Affiliation.admin : 
					deskRoom.isMember(userBareJID) ? Affiliation.member : Affiliation.none;
	}
}
