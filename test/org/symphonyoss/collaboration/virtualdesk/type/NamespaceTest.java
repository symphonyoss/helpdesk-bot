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

package org.symphonyoss.collaboration.virtualdesk.type;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NamespaceTest
{
	@Test
	public void test_Constant()
	{
		assertEquals(Namespace.DISCO_INFO, "http://jabber.org/protocol/disco#info");
		assertEquals(Namespace.DISCO_ITEMS, "http://jabber.org/protocol/disco#items");
		assertEquals(Namespace.ROOM_INFO, "http://jabber.org/protocol/muc#roominfo");
		assertEquals(Namespace.IQ_GATEWAY, "jabber:iq:gateway");
		assertEquals(Namespace.IQ_GATEWAY_REGISTER, "jabber:iq:gateway:register");
		assertEquals(Namespace.IQ_LAST, "jabber:iq:last");
		assertEquals(Namespace.IQ_REGISTER, "jabber:iq:register");
		assertEquals(Namespace.IQ_REGISTERED, "jabber:iq:registered");
		assertEquals(Namespace.IQ_ROSTER, "jabber:iq:roster");
		assertEquals(Namespace.IQ_VERSION, "jabber:iq:version");
		assertEquals(Namespace.IQ_PING, "urn:xmpp:ping");
		assertEquals(Namespace.CHATSTATES, "http://jabber.org/protocol/chatstates");
		assertEquals(Namespace.XEVENT, "jabber:x:event");
		assertEquals(Namespace.XDATA, "jabber:x:data");
		assertEquals(Namespace.MUC, "http://jabber.org/protocol/muc");
		assertEquals(Namespace.MUC_USER, "http://jabber.org/protocol/muc#user");
		assertEquals(Namespace.MUC_ADMIN, "http://jabber.org/protocol/muc#admin");
		assertEquals(Namespace.SPARKNS, "http://www.jivesoftware.com/spark");
		assertEquals(Namespace.DELAY, "urn:xmpp:delay");
		assertEquals(Namespace.OFFLINE, "http://jabber.org/protocol/offline");
		assertEquals(Namespace.X_DELAY, "jabber:x:delay");
		assertEquals(Namespace.VCARD_TEMP, "vcard-temp");
		assertEquals(Namespace.VCARD_TEMP_X_UPDATE, "vcard-temp:x:update");
		assertEquals(Namespace.ATTENTIONNS, "urn:xmpp:attention:0");
		assertEquals(Namespace.STANZA_ERROR, "urn:ietf:params:xml:ns:xmpp-stanzas");
		assertEquals(Namespace.MUC_OWNER, "http://jabber.org/protocol/muc#owner");
		assertEquals(Namespace.MUC_ROOM_CONFIG, "http://jabber.org/protocol/muc#roomconfig");
	}
}
