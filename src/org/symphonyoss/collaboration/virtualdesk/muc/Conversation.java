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

import org.symphonyoss.collaboration.virtualdesk.data.UserState;


public class Conversation extends Desk
{
	private Desk parent;
	
	private boolean hasMemberJoined;
	private boolean hasParticipantJoined;
	
	private boolean memberRejectInvite;
	private boolean participantRejectInvite;
	
	private IDeskDirectory deskDirectory;
	
	private UserState question;
	
	public Conversation(String name, String domain, Desk parent)
	{
		super(name, domain);
		
		this.parent = parent;
		
		hasMemberJoined = false;
		hasParticipantJoined = false;
		
		memberRejectInvite = false;
		participantRejectInvite = false;
	}
	
	public Desk getParentDesk()
	{
		return parent;
	}
	
	public boolean getMemberJoined()
	{
		return hasMemberJoined;
	}
	
	public boolean getParticipantJoined()
	{
		return hasParticipantJoined;
	}
	
	public void setMemberJoinedState()
	{
		hasMemberJoined = true;
	}
	
	public void setParticipantJoinedState()
	{
		hasParticipantJoined = true;
	}
	
	public void setQuestion(UserState question)
	{
		this.question = question;
	}
	
	public UserState getQuestion()
	{
		return question;
	}
	
	public void memberRejectInvite()
	{
		memberRejectInvite = true;
	}
	
	public void participantRejectInvite()
	{
		participantRejectInvite = true;
	}
	
	public boolean getMemberRejectInvite()
	{
		return memberRejectInvite;
	}
	
	public boolean getParticipantRejectInvite()
	{
		return participantRejectInvite;
	}

	public IDeskDirectory getDeskDirectory()
	{
		return deskDirectory;
	}

	public void setDeskDirectory(IDeskDirectory deskDirectory)
	{
		this.deskDirectory = deskDirectory;
	}
	
	public boolean closeConversation()
	{
		boolean questionClosed = canCloseQuestion();
		
		if (questionClosed)
		{
			parent.closeQuestion(question.getPosterNickname());
			
			if (deskDirectory != null)
			{
				deskDirectory.removeDeskConversation(this.getName());
			}
		}
		else
		{
			parent.resetQuestionState(question.getPosterNickname());
			
			if ((getMemberRejectInvite() || getMemberJoined()) && (getParticipantRejectInvite() || getParticipantJoined()))
			{
				deskDirectory.removeDeskConversation(getName());
			}
		}
		
		return questionClosed;
	}
	
	private boolean canCloseQuestion()
	{
		return hasMemberJoined && hasParticipantJoined;
	}
}
