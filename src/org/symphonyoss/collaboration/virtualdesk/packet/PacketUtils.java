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

import org.dom4j.Element;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;

public final class PacketUtils
{
	public static String getIQNamespace(IQ iq)
	{
		Element queryElement = iq.getChildElement();

		return queryElement.getNamespaceURI();
	}

	public static boolean hasNamespace(Message message, String namespace)
	{
		Element xElement = message.getChildElement("x", namespace);

		return xElement != null;
	}
	
	public static boolean isDestroyElement(IQ iq)
	{
		Element queryElement = iq.getChildElement();
		
		Element destroyElement = queryElement.element("destroy");
		
		return destroyElement != null;
	}
	
	public static boolean isInviteReject(Message message)
	{
		if (hasNamespace(message, Namespace.MUC_USER))
		{
			Element xElement = message.getChildElement("x", Namespace.MUC_USER);
			
			return xElement.element("decline") != null;
		}
		
		return false;
	}
}
