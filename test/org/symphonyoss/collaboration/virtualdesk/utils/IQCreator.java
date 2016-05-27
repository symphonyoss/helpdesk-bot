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

package org.symphonyoss.collaboration.virtualdesk.utils;

import org.dom4j.Element;
import org.symphonyoss.collaboration.virtualdesk.data.RoomProperty;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

public class IQCreator
{
	public static IQ createIQResponseRoomSetting(User actor, Desk deskRoom)
	{
		IQ iq = new IQ();
		iq.setType(Type.get);
		iq.setFrom(actor.getJID());
		iq.setTo(deskRoom.getJID());
		iq.setChildElement("query", Namespace.MUC_OWNER);

		Element queryElement = iq.getChildElement();
		
		Element xElement = queryElement.addElement("x", Namespace.XDATA).addAttribute("type", "submit");
		
		xElement.addElement("field")
				.addAttribute("var", "FORM_TYPE")
				.addAttribute("type", "hidden")
				.addElement("value").setText(Namespace.MUC_ROOM_CONFIG);

		xElement.addElement("field")
				.addAttribute("var", RoomProperty.RoomName.getId())
				.addAttribute("type", RoomProperty.RoomName.getType())
				.addElement("value").setText(deskRoom.getName());

		xElement.addElement("field")
				.addAttribute("var", RoomProperty.Description.getId())
				.addAttribute("type", RoomProperty.Description.getType())
				.addElement("value").setText(deskRoom.getDescription());

		xElement.addElement("field")
				.addAttribute("var", RoomProperty.PersistentRoom.getId())
				.addAttribute("type", RoomProperty.PersistentRoom.getType())
				.addElement("value").setText(deskRoom.isPersistent() ? "1" : "0");

		xElement.addElement("field")
				.addAttribute("var", RoomProperty.MembersOnly.getId())
				.addAttribute("type", RoomProperty.MembersOnly.getType())
				.addElement("value").setText(deskRoom.isMembersOnly() ? "1" : "0");

		if(deskRoom.getParticipants().size() > 0)
		{
			Element memberElement = xElement.addElement("field")
					.addAttribute("var", RoomProperty.RoomParticipants.getId())
					.addAttribute("type", RoomProperty.RoomParticipants.getType());
					for(String val : deskRoom.getParticipants()){
						memberElement.addElement("value").setText(val);
					}
		}
		
		if(deskRoom.getMembers().size() > 0)
		{
			Element memberElement = xElement.addElement("field")
					.addAttribute("var", RoomProperty.RoomMembers.getId())
					.addAttribute("type", RoomProperty.RoomMembers.getType());
			for(String val : deskRoom.getMembers()){
				memberElement.addElement("value").setText(val);
			}
		}
		
		if(deskRoom.getAdmins().size() > 0)
		{
			Element adminElement = xElement.addElement("field")
				.addAttribute("var", RoomProperty.RoomAdmins.getId())
				.addAttribute("type", RoomProperty.RoomAdmins.getType());
				for(String val : deskRoom.getAdmins()){
					adminElement.addElement("value").setText(val);
				}
		}
		
		if(deskRoom.getOwners().size() > 0)
		{
			Element ownerElement = xElement.addElement("field")
				.addAttribute("var", RoomProperty.RoomOwners.getId())
				.addAttribute("type", RoomProperty.RoomOwners.getType());
				for(String val : deskRoom.getOwners()){
					ownerElement.addElement("value").setText(val);
				}
		}
		
		if (!deskRoom.getName().equals(deskRoom.getDeskAliasName()))
		{
			xElement.addElement("field")
				.addAttribute("var", RoomProperty.DeskAlias.getId())
				.addAttribute("type", RoomProperty.DeskAlias.getType())
				.addElement("value").setText(deskRoom.getDeskAliasName());
		}
		
		return iq;
	}
	

}
