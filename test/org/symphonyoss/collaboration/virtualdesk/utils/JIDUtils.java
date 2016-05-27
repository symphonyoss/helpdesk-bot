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

import org.xmpp.packet.JID;

public class JIDUtils
{
	public static String getUserBareJID(String name)
	{
		return String.format("%s@%s", name, TestConst.VIRTUALDESK_DOMAIN);
	}
	
	public static JID getUserJID(String name)
	{
		return new JID(name, TestConst.VIRTUALDESK_DOMAIN, null);
	}
	
	public static JID getNicknameJID(String deskName, String nickname)
	{
		return new JID(deskName, TestConst.VIRTUALDESK_DOMAIN, nickname);
	}
}
