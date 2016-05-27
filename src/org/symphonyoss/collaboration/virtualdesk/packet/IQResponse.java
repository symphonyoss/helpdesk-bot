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
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError.Condition;
import org.symphonyoss.collaboration.virtualdesk.data.RoomProperty;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IQResponse
{
	private static Logger Logger = LoggerFactory.getLogger(IQResponse.class);
	
	public static Packet createErrorResponse(IQ iq, Condition condition)
	{
		
		IQ errorIQ = IQ.createResultIQ(iq);

		errorIQ.setType(Type.error);

		errorIQ.setChildElement((Element) iq.getChildElement().clone());
		errorIQ.setError(condition);

		return errorIQ;
	}

	public static Packet createRoomListing(IQ iq, Collection <Desk> desks)
	{
		IQ roomListingResult = IQ.createResultIQ(iq);

		roomListingResult.setChildElement((Element) iq.getChildElement().clone());

		Element queryElement = roomListingResult.getChildElement();

		for (Desk desk : desks)
		{
			Element deskItem = queryElement.addElement("item");

			deskItem.addAttribute("jid", desk.getJID().toString());
			deskItem.addAttribute("name", desk.getNaturalName());
		}

		return roomListingResult;
	}

	public static Packet createGetDeskForm(IQ iq, Desk deskRoom)
	{
		Logger.debug("Creating desk properties packet IQ response");
		
		IQ getDeskFormResponse = IQ.createResultIQ(iq);

		getDeskFormResponse.setChildElement((Element) iq.getChildElement().clone());

		Element queryElement = getDeskFormResponse.getChildElement();

		Element xElement = queryElement.addElement("x", Namespace.XDATA).addAttribute("type", "form");

		xElement.addElement("title").setText("Desk configuration");
		xElement.addElement("instructions").addText(String.format(
				"The desk %s has been created. To accept the default configuration, click the \"OK\" button. Or, modify the settings by completing the following form", deskRoom.getName()));

		xElement.addElement("field")
				.addAttribute("var", "FORM_TYPE")
				.addAttribute("type", "hidden")
				.addElement("value").setText(Namespace.MUC_ROOM_CONFIG);

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.RoomName.getLabel())
				.addAttribute("var", RoomProperty.RoomName.getId())
				.addAttribute("type", RoomProperty.RoomName.getType())
				.addElement("value").setText(deskRoom.getNaturalName());

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.Description.getLabel())
				.addAttribute("var", RoomProperty.Description.getId())
				.addAttribute("type", RoomProperty.Description.getType())
				.addElement("value").setText(deskRoom.getDescription());
		
		xElement.addElement("field")
				.addAttribute("label", RoomProperty.DeskAlias.getLabel())
				.addAttribute("var", RoomProperty.DeskAlias.getId())
				.addAttribute("type", RoomProperty.DeskAlias.getType())
				.addElement("value").setText(JID.escapeNode(deskRoom.getDeskAliasName()));

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.PersistentRoom.getLabel())
				.addAttribute("var", RoomProperty.PersistentRoom.getId())
				.addAttribute("type", RoomProperty.PersistentRoom.getType())
				.addElement("value").setText("1");

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.MembersOnly.getLabel())
				.addAttribute("var", RoomProperty.MembersOnly.getId())
				.addAttribute("type", RoomProperty.MembersOnly.getType())
				.addElement("value").setText(deskRoom.isMembersOnly() ? "1" : "0");
		
		xElement.addElement("field")
				.addAttribute("label", RoomProperty.RoomParticipants.getLabel())
				.addAttribute("var", RoomProperty.RoomParticipants.getId())
				.addAttribute("type", RoomProperty.RoomParticipants.getType())
				.addElement("value").setText(StringUtils.join(deskRoom.getParticipants(), ","));

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.RoomMembers.getLabel())
				.addAttribute("var", RoomProperty.RoomMembers.getId())
				.addAttribute("type", RoomProperty.RoomMembers.getType())
				.addElement("value").setText(StringUtils.join(deskRoom.getMembers(), ","));

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.RoomAdmins.getLabel())
				.addAttribute("var", RoomProperty.RoomAdmins.getId())
				.addAttribute("type", RoomProperty.RoomAdmins.getType())
				.addElement("value").setText(StringUtils.join(deskRoom.getAdmins(), ","));

		xElement.addElement("field")
				.addAttribute("label", RoomProperty.RoomOwners.getLabel())
				.addAttribute("var", RoomProperty.RoomOwners.getId())
				.addAttribute("type", RoomProperty.RoomOwners.getType())
				.addElement("value").setText(StringUtils.join(deskRoom.getOwners(), ","));

		Logger.debug("Return desk form response");
		return getDeskFormResponse;
	}

	public static Packet createGetOccupantList(IQ iq, Collection <User> occupants)
	{
		IQ participantList = IQ.createResultIQ(iq);

		participantList.setChildElement((Element) iq.getChildElement().clone());

		Element queryElement = participantList.getChildElement();

		String deskBareJID = iq.getTo().toBareJID();

		for (User occupant : occupants)
		{
			queryElement.addElement("item").addAttribute("jid",
					String.format("%s/%s", deskBareJID, occupant.getNickname()));
		}

		return participantList;
	}

	public static Packet createDeskInfo(IQ iq, String deskName, String description, int numberOfOccupants)
	{
		IQ deskInfo = IQ.createResultIQ(iq);

		deskInfo.setChildElement((Element) iq.getChildElement().clone());

		Element queryElement = deskInfo.getChildElement();

		Element identityElement = queryElement.addElement("identity");
		identityElement.addAttribute("category", "virtualdesk");
		identityElement.addAttribute("name", "virtualdesk");
		identityElement.addAttribute("category", deskName);
		identityElement.addAttribute("type", "text");

		queryElement.addElement("feature").addAttribute("var", Namespace.MUC);
		queryElement.addElement("feature").addAttribute("var", "muc_public");
		queryElement.addElement("feature").addAttribute("var", "muc_open");
		queryElement.addElement("feature").addAttribute("var", "muc_unmoderated");
		queryElement.addElement("feature").addAttribute("var", "muc_anonymous");
		queryElement.addElement("feature").addAttribute("var", "muc_temporary");

		Element xElement = queryElement.addElement("x", Namespace.XDATA).addAttribute("type", "result");

		Element fieldTypeElement = xElement.addElement("field").addAttribute("var", "FORM_TYPE").addAttribute("type", "hidden");
		fieldTypeElement.addElement("value").setText(Namespace.ROOM_INFO);

		Element descriptionElement = xElement.addElement("field").addAttribute("label", "Description").addAttribute("var", "muc#roominfo_description");
		
		descriptionElement.addElement("value").addText(description);
		
		Element numOfOccupantsElement = xElement.addElement("field").addAttribute("label", "Number of occupants").addAttribute("var", "muc#roominfo_occupants");

		numOfOccupantsElement.addElement("value").addText(Integer.toString(numberOfOccupants));

		return deskInfo;
	}

	public static Packet createVirtualDeskServiceInfo(IQ iq)
	{
		IQ serviceInfoResult = IQ.createResultIQ(iq);

		serviceInfoResult.setChildElement((Element) iq.getChildElement().clone());

		Element queryElement = serviceInfoResult.getChildElement();

		Element[] identities = getIdentities();

		for (Element identity : identities)
		{
			identity.setQName(new QName(identity.getName(), queryElement.getNamespace()));
			queryElement.add((Element) identity.clone());
		}

		Element[] features = getFeatures();
		for (Element feature : features)
		{
			feature.setQName(new QName(feature.getName(), queryElement.getNamespace()));
			queryElement.add((Element) feature.clone());
		}

		return serviceInfoResult;
	}

	private static Element[] getIdentities()
	{
		ArrayList <Element> identities = new ArrayList <Element>();

		Element identity = DocumentHelper.createElement("identity");
		identity.addAttribute("category", "conference");
		identity.addAttribute("name", "Virtual Desk Service");
		identity.addAttribute("type", "text");
		identities.add(identity);

//		Element searchId = DocumentHelper.createElement("identity");
//		searchId.addAttribute("category", "directory");
//		searchId.addAttribute("name", "Virtual Desk Search");
//		searchId.addAttribute("type", "chatroom");
//		identities.add(searchId);

		return identities.toArray(new Element[identities.size()]);
	}

	private static Element[] getFeatures()
	{
		ArrayList <Element> features = new ArrayList <Element>();

		Element identity1 = DocumentHelper.createElement("feature");
		identity1.addAttribute("var", "http://jabber.org/protocol/muc/");
		features.add(identity1);

//		Element identity2 = DocumentHelper.createElement("feature");
//		identity2.addAttribute("var", Namespace.DISCO_INFO);
//		features.add(identity2);
//
//		Element identity3 = DocumentHelper.createElement("feature");
//		identity3.addAttribute("var", Namespace.DISCO_ITEMS);
//		features.add(identity3);

		return features.toArray(new Element[features.size()]);
	}
}
