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
import java.util.Date;
import java.util.List;
import org.dom4j.Element;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public final class MessageResponse
{
	public static Packet createDeskMessageResponse(String message, User to, JID deskJID)
	{
		Message deskMessage = new Message();
		deskMessage.setFrom(deskJID.toBareJID());
		deskMessage.setTo(to.getJID());
		deskMessage.setType(Message.Type.groupchat);

		deskMessage.setBody(message);

		return deskMessage;
	}

	public static Collection <Packet> createDeskMessageResponse(String message, Collection <User> toList, JID deskJID)
	{
		List <Packet> responseMessageList = new ArrayList <Packet>();

		for (User to : toList)
		{
			responseMessageList.add(createDeskMessageResponse(message, to, deskJID));
		}

		return responseMessageList;
	}

	public static Packet createHistoryMessage(String senderNickname, String toJID, String body, Date timestamp, JID deskJID)
	{
		Message message = new Message();
		
		String senderDeskJID = getNicknameJID(deskJID, senderNickname);
		
		message.setFrom(senderDeskJID);
		message.setTo(toJID);
		message.setID(null);
		message.setType(Message.Type.groupchat);
		
		message.setBody(body);
		
		String messageTimestamp = DateUtils.formatMessageTimestamp(timestamp);
		
		Element delayElement = (Element)message.addChildElement("delay", Namespace.DELAY);
		delayElement.addAttribute("from", senderDeskJID);
		delayElement.addAttribute("stamp", messageTimestamp);
		Element xElement = (Element)message.addChildElement("x", Namespace.X_DELAY);
		xElement.addAttribute("from", senderDeskJID);
		xElement.addAttribute("stamp", messageTimestamp);
	
		return message;
	}
	
	public static Collection <Packet> createMessageResponse(User from, Collection <User> toList, JID deskJID, Message message)
	{
		List <Packet> responseMessageList = new ArrayList <Packet>();

		for (User to : toList)
		{
			Message responseMessage = (Message)createMessageResponse(from, to, deskJID, message); 

			responseMessageList.add(responseMessage);
		}

		return responseMessageList;
	}
	
	public static Packet createMessageResponse(User from, User to, JID deskJID, Message message)
	{
		String senderNicknameBareJID = String.format("%s/%s", deskJID.toBareJID(), from.getNickname());

		Message responseMessage = new Message(); 
		
		responseMessage.setID(message.getID());
		responseMessage.setFrom(senderNicknameBareJID);
		responseMessage.setTo(to.getJID());
		responseMessage.setType(Message.Type.groupchat);
		responseMessage.setBody(message.getBody());

		return responseMessage;
	}

	public static Packet createJoinConversationInvite(User senderUser, String newDeskRoomID, Desk desk)
	{
		return createJoinConversationInvite(senderUser.getJID().toString(), newDeskRoomID, desk);
	}
	
	public static Packet createJoinConversationInvite(String senderJID, String newDeskRoomID, Desk desk)
	{
		Message deskMessage = new Message();
		deskMessage.setTo(senderJID);
		deskMessage.setFrom(newDeskRoomID);
		
		Element mucUserForm = deskMessage.addChildElement("x", Namespace.MUC_USER);
		Element invite = mucUserForm.addElement("invite").addAttribute("from", newDeskRoomID);
		invite.addAttribute("reason", "Please join conference");
		
		return deskMessage;
	}
	
	private static String getNicknameJID(JID roomJID, String nickname)
	{
		return String.format("%s/%s", roomJID.toBareJID(), nickname);
	}
}
