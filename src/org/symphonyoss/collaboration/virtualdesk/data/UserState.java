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

package org.symphonyoss.collaboration.virtualdesk.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserState
{
	private String posterNickname;
	private String posterJID;
	
	private Date timestamp;
	
	private List <String> questionList;

	private WorkflowState state;

	public UserState()
	{
		questionList = new ArrayList <String>();
		
		state = WorkflowState.AwaitResponse;
		
		timestamp = new Date();
	}
	
	public UserState(String posterNickname, String posterJID, String question)
	{
		this();
		
		this.posterNickname = posterNickname;
		this.posterJID = posterJID;
		
		questionList.add(question);
	}

	public void addQuestion(String question)
	{
		questionList.add(question);
	}

	public String getPosterNickname()
	{
		return posterNickname;
	}
	
	public void setPosterNickname(String posterNickname)
	{
		this.posterNickname = posterNickname;
	}
	
	public String getPosterJID()
	{
		return posterJID;
	}
	
	public void setPosterJID(String posterJID)
	{
		this.posterJID = posterJID;
	}
	
	public List <String> getQuestions()
	{
		return questionList;
	}
	
	public void setQuestions(List<String> questionList)
	{
		this.questionList = questionList;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(Date timstamp)
	{
		this.timestamp = timstamp;
	}
	
	public WorkflowState getState()
	{
		return state;
	}

	public void setState(WorkflowState state)
	{
		this.state = state;
	}
}
