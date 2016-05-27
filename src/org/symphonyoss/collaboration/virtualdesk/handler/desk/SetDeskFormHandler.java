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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.RoomProperty;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.muc.DeskSetting;
import org.symphonyoss.collaboration.virtualdesk.muc.IDeskDirectory;
import org.symphonyoss.collaboration.virtualdesk.packet.IQResponse;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetDeskFormHandler extends AbstractActionHandler
{
	private static Logger Logger = LoggerFactory.getLogger(SetDeskFormHandler.class);
	
	private IQ iqRequest;

	private Desk deskRoom;

	private IDeskDirectory deskDirectory;

	public SetDeskFormHandler(IQ iqRequest, Desk deskRoom, IDeskDirectory deskDirectory)
	{
		this.iqRequest = iqRequest;
		this.deskRoom = deskRoom;
		this.deskDirectory = deskDirectory;
	}

	@Override
	protected void internalHandle()
	{
		MDC.put("deskjid", deskRoom.getJID());
		
		Logger.debug("Starting setting desk properties");
		
		Set <String> owners = deskRoom.getOwners();

		String senderBareJID = iqRequest.getFrom().toBareJID();

		// Allow only owner to change the desk setting
		if (owners.contains(senderBareJID))
		{
			DeskSetting settings = DeskSetting.parse(iqRequest);
			
			if (!validateDeskSetting(settings))
			{
				return;
			}
			
			User virtualDeskUser = deskRoom.getVirtualDeskUser();
			PresenceType currentRoomPresence = deskRoom.getDeskPresence();
			
			setDeskSetting(settings);

			IQ setDeskFormResponse = IQ.createResultIQ(iqRequest);
			setDeskFormResponse.setType(Type.result);
			
			//sent result IQ to actor
			packetList.add(setDeskFormResponse);
			
			//sent permission changed to occupant
			packetList.addAll(new PermissionChangedHandler(deskRoom).handle());
			
			// Send Virtual Desk name alias change to participant
			packetList.addAll(new VirtualDeskUserNicknameChangedHandler(virtualDeskUser, deskRoom.getVirtualDeskUser(), deskRoom).handle());
			
			//Check New desk presence, notify participant in case desk presence changed
			PresenceType newDeskPresence = deskRoom.getDeskPresence();
			if (!currentRoomPresence.equals(newDeskPresence))
			{
				packetList.addAll(PresenceResponse.createPresenceUpdate(virtualDeskUser, deskRoom.getCurrentParticipants(), deskRoom.getJID(), newDeskPresence));
			}
			
			Logger.info("{} has updated desk [{}].", iqRequest.getFrom(), deskRoom.getJID());
		}
		else
		{
			Logger.info("{} cannot update desk form because user is not desk owner of desk [{}].", 
					iqRequest.getFrom(), deskRoom.getJID());
			packetList.add(IQResponse.createErrorResponse(iqRequest, PacketError.Condition.not_allowed));
		}
		
		Logger.debug("Finished setting desk properties");
	}

	private boolean validateDeskSetting(DeskSetting settings)
	{
		boolean hasError = false;
		List<String> errorMessageList = new ArrayList<String>();
		
		// Check room owner that must not be empty
		List <String> ownervals = settings.getSettings().get(RoomProperty.RoomOwners.getId());
		if(ownervals == null || ownervals.size() == 0 )
		{
			Logger.info("Updating desk with no owner: Removing all room owner are not allowed");
			hasError = true;
			
			errorMessageList.add("Removing all room owners is not allowed");
		}
		
		List <String> deskAliasNameValue = settings.getSettings().get(RoomProperty.DeskAlias.getId());
		if (deskAliasNameValue != null)
		{
			String deskAliasName = JID.unescapeNode(deskAliasNameValue.get(0));
			
			// Check desk alias name must not be empty
			if (deskAliasName.trim().length() <= 0)
			{
				Logger.info("Cannot set the new desk alias name because it is empty or contains whitespaces only.");
				hasError = true;
				
				errorMessageList.add("Desk alias cannot be empty or whitespaces");
			}
			else if (deskRoom.getOccupantByNickname(deskAliasName) != null)
			{
				Logger.info("Cannot set the new desk alias name because nickname has already been in used by other occupants.");
				hasError = true;
				
				errorMessageList.add("Desk alias has already been in used by other occupants.");
			}
		}
		
		if (hasError)
		{
			String errorMessage = StringUtils.join(errorMessageList, "\n");
			
			IQ errorIQ = IQ.createResultIQ(iqRequest);
			errorIQ.setChildElement("query", Namespace.MUC_OWNER);
			errorIQ.setError(new PacketError(PacketError.Condition.not_acceptable, PacketError.Type.cancel, errorMessage));
			packetList.add(errorIQ);
		}
		
		return !hasError;
	}
	
	private void setDeskSetting(DeskSetting settings)
	{
		Logger.debug("Updating desk properties");
		
		Set <String> participantSet = new LinkedHashSet <String>();
		Set <String> memberSet = new LinkedHashSet <String>();
		Set <String> adminSet = new LinkedHashSet <String>();
		Set <String> ownerSet = new LinkedHashSet <String>();

		for (Entry <String, List <String>> setting : settings.getSettings().entrySet())
		{
			if (setting.getKey().equalsIgnoreCase(RoomProperty.Description.getId()))
			{
				deskRoom.setDescription(setting.getValue().get(0));
			}
			else if (setting.getKey().equalsIgnoreCase(RoomProperty.MembersOnly.getId()))
			{
				deskRoom.isMembersOnly(setting.getValue().get(0).equals("1") ? true : false);
			}
			else if (setting.getKey().equalsIgnoreCase(RoomProperty.RoomParticipants.getId()))
			{
				participantSet = extractUserList(setting.getValue());
			}
			else if (setting.getKey().equalsIgnoreCase(RoomProperty.RoomMembers.getId()))
			{
				memberSet = extractUserList(setting.getValue());
			}
			else if (setting.getKey().equalsIgnoreCase(RoomProperty.RoomAdmins.getId()))
			{
				adminSet = extractUserList(setting.getValue());
			}
			else if (setting.getKey().equalsIgnoreCase(RoomProperty.RoomOwners.getId()))
			{
				ownerSet = extractUserList(setting.getValue());
			}
			else if (setting.getKey().equalsIgnoreCase(RoomProperty.DeskAlias.getId()))
			{
				String newNickname = JID.unescapeNode(setting.getValue().get(0));
				
				deskRoom.setDeskAliasName(newNickname.trim());
			}
		}

		deskRoom.syncParticipant(participantSet);
		deskRoom.syncMembers(memberSet);
		deskRoom.syncAdmins(adminSet);
		deskRoom.syncOwners(ownerSet);
		deskDirectory.updateDeskSetting(deskRoom);
		Logger.debug("finished updating desk properties");
	}

	private Set <String> extractUserList(List <String> listOfUsers)
	{
		Set <String> userSet = new LinkedHashSet <String>();

		for (String user : listOfUsers)
		{
			if(StringUtils.isNotEmpty(user) && user.contains("@"))
			{
				try
				{
					JID jid = new JID(user.toLowerCase().trim());
					userSet.add(jid.toBareJID());
				}
				catch(Exception ex)
				{
					Logger.debug("Skip user: {}, Invalid JID format", user);
				}
			}
			else
			{
				Logger.debug("Skip user: {}, Invalid JID format", user);
			}
		}

		return userSet;
	}
}
