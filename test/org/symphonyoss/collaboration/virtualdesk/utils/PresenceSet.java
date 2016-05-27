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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Type;

public class PresenceSet
{
	private Map<String, Integer> packetToCount;
	private Map<String, List<ExtractPresence>> packetList; 
	
	public PresenceSet()
	{
		packetToCount = new HashMap<>();
		packetList = new HashMap<>();
	}
	
	public void addPresence(Presence presence)
	{
		String to = presence.getTo().toBareJID();
		if(packetToCount.containsKey(to))
		{
			packetToCount.put(to, packetToCount.get(to).intValue() + 1);
			List<ExtractPresence> list = packetList.get(to);
			list.add(new ExtractPresence(presence));
		}
		else
		{
			packetToCount.put(to, 1);
			List<ExtractPresence> list = new ArrayList<>();
			list.add(new ExtractPresence(presence));
			packetList.put(to, list);
		}
	}
	
	public int getPacketCount(JID jid)
	{
		if(packetToCount.containsKey(jid.toBareJID()))
		{
			return packetToCount.get(jid.toBareJID()).intValue();
		}
		
		return 0;
	}
	
	public List<ExtractPresence> getPresences(JID jid)
	{
		if(packetList.containsKey(jid.toBareJID()))
		{
			return packetList.get(jid.toBareJID());
		}
		
		return null;
	}
	
	public class ExtractPresence
	{
		public JID to;
		public JID from;
		public String message;
		public Role role;
		public Affiliation affiliation;
		public Type type;
		public String nickName;
		
		public ExtractPresence(Presence presence)
		{
			extractPacket(presence);
		}
		
		private void extractPacket(Presence presence)
		{
			this.from = presence.getFrom();
			this.to = presence.getTo();
			this.type = presence.getType();
			this.nickName = presence.getFrom().getResource();
			
			Element queryElement = presence.addChildElement("x", Namespace.MUC_USER);
			XPath fieldXPath = (XPath) queryElement.createXPath("//x:item");
			
			Map <String, String> namespaceUris = new HashMap <String, String>();
			namespaceUris.put("x", Namespace.MUC_USER);

			fieldXPath.setNamespaceURIs(namespaceUris);

			Element fieldElement = (Element) fieldXPath.selectNodes(queryElement).get(0);
			this.role = Role.parse(fieldElement.attributeValue("role"));
			this.affiliation = Affiliation.parse(fieldElement.attributeValue("affiliation"));
			//this.nickName = fieldElement.attributeValue("nick");
		}
		
	}
}
