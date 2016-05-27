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

package org.symphonyoss.collaboration.virtualdesk.muc;

import java.util.Collection;

public interface IDeskDirectory
{
	Desk getDesk(String deskName);
	
	Conversation getConversation(String deskName);

	boolean contains(String deskName);
	
	boolean containsConversation(String conversationName);

	Collection <Desk> getAllDesks();
	
	Collection <Conversation> getAllConversation();

	Desk createDesk(String deskName, String creatorBareJID);
	
	Conversation createDeskConversation(String deskName, String creatorBareJID, Desk parent);
	
	void removeDeskConversation(String deskName);
	
	void updateDeskSetting(Desk deskRoom);

	int getCurrentNumberOfDeskCreated(String userBareJID);
	
	void destroyDesk(Desk deskRoom);
}
