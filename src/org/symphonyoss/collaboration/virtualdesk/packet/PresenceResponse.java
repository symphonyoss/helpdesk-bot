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

package org.symphonyoss.collaboration.virtualdesk.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Type;

public final class PresenceResponse
{
	public static Packet createPresenceError(User from, JID deskJID, Condition condition)
	{
		Presence presenceError = new Presence();
		presenceError.setFrom(getNicknameJID(deskJID, from.getNickname()));
		presenceError.setTo(from.getJID());
		presenceError.setType(Type.error);

		presenceError.setError(condition);

		return presenceError;
	}

	public static Packet createPresenceError(Presence presence, Condition condition)
	{
		Presence presenceError = new Presence();
		presenceError.setFrom(presence.getTo());
		presenceError.setTo(presence.getFrom());
		presenceError.setType(Type.error);

		presenceError.setError(condition);

		return presenceError;
	}

	public static Packet createPresenceError(Presence presence, Condition condition, String message)
	{
		Presence presenceError = new Presence();
		presenceError.setFrom(presence.getTo());
		presenceError.setTo(presence.getFrom());
		presenceError.setType(Type.error);

		presenceError.setError(condition);
		
		presenceError.getError().setText(message);

		return presenceError;
	}

	public static Collection<Packet> createNicknameChange(User from, String oldNickname, Collection<User> toList, JID deskJID)
	{
		String oldFromJID = getNicknameJID(deskJID, oldNickname);
		
		List <Packet> nicknameChangeList = new ArrayList <Packet>();
		
		for (User to : toList)
		{
			Presence nicknameChangePresence = new Presence();
			nicknameChangePresence.setFrom(oldFromJID);
			nicknameChangePresence.setTo(to.getJID());
			nicknameChangePresence.setType(Presence.Type.unavailable);
			
			Element xElement = nicknameChangePresence.addChildElement("x", Namespace.MUC_USER);
			Element itemElement = xElement.addElement("item");
			xElement.addElement("status").addAttribute("code", "303");
			
			itemElement.addAttribute("affilation", from.getAffiliation().name());
			itemElement.addAttribute("nick", from.getNickname());
			itemElement.addAttribute("role", from.getRole().name());
			
			itemElement.addElement("reason");
			itemElement.addElement("actor").addAttribute("jid", "");
			
			nicknameChangeList.add(nicknameChangePresence);
		}
		
		return nicknameChangeList;
	}
	
	public static Packet createKickUser(User user, JID deskJID, String reason)
	{
		Presence kick = new Presence();
		kick.setFrom(getNicknameJID(deskJID, user.getNickname()));
		kick.setTo(user.getJID());
		kick.setType(Presence.Type.unavailable);

		Element xElement = kick.addChildElement("x", Namespace.MUC_USER);

		Element itemElement = xElement.addElement("item");
		itemElement.addAttribute("affiliation", "none");
		itemElement.addAttribute("role", "none");

		itemElement.addElement("actor").addAttribute("nick", deskJID.getNode());

		if (reason != null && reason.length() > 0)
		{
			itemElement.addElement("reason", reason);
		}

		xElement.addElement("status").addAttribute("code", "307");

		return kick;
	}

	public static Collection <Packet> createPermissionChange(User from, Collection <User> toList, JID deskJID, String reason)
	{
		List <Packet> permissionChangeList = new ArrayList <Packet>();

		for (User to : toList)
		{
			if (!to.getNickname().equalsIgnoreCase(from.getNickname()))
			{
				permissionChangeList.add(createPermissionChange(from, to, deskJID, reason));
			}
		}

		return permissionChangeList;
	}

	public static Packet createPermissionChange(User from, User to, JID deskJID, String reason)
	{
		Presence permissionChange = new Presence();
		permissionChange.setFrom(getNicknameJID(deskJID, from.getNickname()));
		permissionChange.setTo(to.getJID());

		Element xElement = permissionChange.addChildElement("x", Namespace.MUC_USER);

		Element itemElement = xElement.addElement("item");
		itemElement.addAttribute("affiliation", from.getAffiliation().name());
		itemElement.addAttribute("nick", from.getNickname());
		itemElement.addAttribute("role", from.getRole().name());

		if (reason != null && reason.length() > 0)
		{
			itemElement.addElement("reason").setText(reason);
		}

		return permissionChange;
	}

	public static Collection <Packet> createPresenceUpdate(User from, Collection <User> toList, JID deskJID, PresenceType presenceType)
	{
		List <Packet> presenceUpdateList = new ArrayList <Packet>();

		for (User to : toList)
		{
			presenceUpdateList.add(createPresenceUpdate(from, to, deskJID, presenceType));
		}

		return presenceUpdateList;
	}

	public static Packet createPresenceUpdate(User from, User to, JID deskJID, PresenceType presenceType)
	{
		Presence presenceUpdate = new Presence();

		presenceUpdate.setFrom(getNicknameJID(deskJID, from.getNickname()));
		presenceUpdate.setTo(to.getJID());

		presenceUpdate.setStatus(presenceType.getStatus());
		presenceUpdate.setPriority(presenceType.getPriority());
		presenceUpdate.setShow(presenceType.getShow());

		Element xElement = presenceUpdate.addChildElement("x", Namespace.MUC_USER);
		Element itemElement = xElement.addElement("item");

		itemElement.addAttribute("affiliation", from.getAffiliation().name());
		itemElement.addAttribute("jid", from.getJID().toBareJID());
		itemElement.addAttribute("role", from.getRole().name());

		itemElement.addElement("reason");
		itemElement.addElement("actor").addAttribute("jid", "");

		return presenceUpdate;
	}

	public static Collection <Packet> createPresenceUpdate(User from, Collection <User> toList, JID deskJID, Presence presence)
	{
		List <Packet> presenceUpdateList = new ArrayList <Packet>();

		for (User to : toList)
		{
			presenceUpdateList.add(createPresenceUpdate(from, to, deskJID, presence));
		}

		return presenceUpdateList;
	}

	public static Packet createPresenceUpdate(User from, User to, JID deskJID, Presence presence)
	{
		Presence presenceUpdate = new Presence();

		presenceUpdate.setFrom(getNicknameJID(deskJID, from.getNickname()));
		presenceUpdate.setTo(to.getJID());

		presenceUpdate.setStatus(presence.getStatus());
		presenceUpdate.setPriority(presence.getPriority());
		presenceUpdate.setShow(presence.getShow());

		Element xElement = presenceUpdate.addChildElement("x", Namespace.MUC_USER);
		Element itemElement = xElement.addElement("item");

		itemElement.addAttribute("affiliation", from.getAffiliation().name());
		itemElement.addAttribute("jid", from.getJID().toBareJID());
		itemElement.addAttribute("role", from.getRole().name());

		itemElement.addElement("reason");
		itemElement.addElement("actor").addAttribute("jid", "");

		return presenceUpdate;
	}
	
	public static Packet createJoinResponse(User from, User to, JID roomJID, boolean isNewlyCreated)
	{
		Presence joinResponse = new Presence();

		joinResponse.setFrom(getNicknameJID(roomJID, from.getNickname()));
		joinResponse.setTo(to.getJID());

		Element mucUserForm = joinResponse.addChildElement("x", Namespace.MUC_USER);

		if (from.getJID().equals(to.getJID()))
		{
			mucUserForm.addElement("status").addAttribute("code", "110");
		}

		if (isNewlyCreated)
		{
			mucUserForm.addElement("status").addAttribute("code", "201");
		}

		Element item = mucUserForm.addElement("item");
		item.addAttribute("affiliation", from.getAffiliation().name());
		item.addAttribute("role", from.getRole().name());

		return joinResponse;
	}

	public static Collection <Packet> createJoinResponse(User from, Collection <User> toList, JID roomJID, boolean isNewlyCreated)
	{
		List <Packet> joinResponseList = new ArrayList <Packet>();

		for (User toUser : toList)
		{
			joinResponseList.add(createJoinResponse(from, toUser, roomJID, isNewlyCreated));
		}

		return joinResponseList;
	}

	public static Collection <Packet> createJoinResponse(Collection <User> fromList, User to, JID roomJID, boolean isNewlyCreated)
	{
		List <Packet> joinResponseList = new ArrayList <Packet>();

		for (User fromUser : fromList)
		{
			joinResponseList.add(createJoinResponse(fromUser, to, roomJID, isNewlyCreated));
		}

		return joinResponseList;
	}
	
	public static Packet createJoinConversationInvite(User to, String roomJID)
	{
		Presence joinResponse = new Presence();

		joinResponse.setTo(to.getJID());

		joinResponse.addChildElement("x", Namespace.MUC);

		return joinResponse;
	}
	
	public static Packet createLeaveResponse(User from, User to, JID roomJID)
	{
		Presence leaveResponse = new Presence();

		leaveResponse.setFrom(getNicknameJID(roomJID, from.getNickname()));
		leaveResponse.setTo(to.getJID());

		leaveResponse.setType(Presence.Type.unavailable);

		Element muc = leaveResponse.addChildElement("x", Namespace.MUC_USER);

		if (from.getJID().equals(to.getJID()))
		{
			muc.addElement("status").addAttribute("code", "110");
		}

		Element item = muc.addElement("item");
		item.addAttribute("affiliation", from.getAffiliation().name());
		item.addAttribute("role", "none");
		item.addAttribute("nick", from.getNickname());

		return leaveResponse;
	}

	public static Collection <Packet> createLeaveResponse(User from, Collection <User> toList, JID roomJID)
	{
		List <Packet> leaveResponseList = new ArrayList <Packet>();

		for (User toUser : toList)
		{
			leaveResponseList.add(createLeaveResponse(from, toUser, roomJID));
		}

		return leaveResponseList;
	}
	
	private static String getNicknameJID(JID roomJID, String nickname)
	{
		return String.format("%s/%s", roomJID.toBareJID(), nickname);
	}
	
	public static Collection <Packet> destroyDeskResponse(Desk desk, String reason)
	{
		List <Packet> userResponseList = new ArrayList <Packet>();
		
		if (reason == null)
		{
			reason = StringUtils.EMPTY;
		}
		
		for (User to : desk.getCurrentMembers())
		{
			userResponseList.add(destroyDeskResponse(desk, to, reason));
		}
		
		for (User to : desk.getCurrentParticipants())
		{
			userResponseList.add(destroyDeskResponse(desk, to, reason));
		}

		return userResponseList;
	}
	
	public static Packet destroyDeskResponse(Desk desk, User to, String reason)
	{
		Presence destroyResponse = new Presence();
		
		JID deskJID = desk.getJID();
		
		destroyResponse.setFrom(getNicknameJID(deskJID, to.getNickname()));
		destroyResponse.setTo(to.getJID());

		destroyResponse.setType(Presence.Type.unavailable);

		Element xElement = destroyResponse.addChildElement("x", Namespace.MUC_USER);

		Element item = xElement.addElement("item");
		item.addAttribute("affiliation", "none");
		item.addAttribute("role", "none");	
		
		Element destroyElement = xElement.addElement("destroy");
		destroyElement.addAttribute("jid", deskJID.toBareJID());
		
		Element reasonElement = destroyElement.addElement("reason");
		reasonElement.setText(reason);
		
		return destroyResponse;
	}
}
