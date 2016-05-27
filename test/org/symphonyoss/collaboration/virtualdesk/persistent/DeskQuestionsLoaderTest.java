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

package org.symphonyoss.collaboration.virtualdesk.persistent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.data.serializer.QuestionSerializer;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskQuestionsLoader;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;
import org.slf4j.Logger;

public class DeskQuestionsLoaderTest
{
	private DeskQuestionsLoader questionLoader;
	
	private @Mocked @NonStrict ResultSet resultSet;
	
	@Before
	public void before()
	{
		questionLoader = new DeskQuestionsLoader();
	}
	
	@Test
	public void processRow_AnyCondition_RetrieveDeskQuestionsFromResult() throws SQLException
	{
		new Expectations()
		{
			@Mocked @NonStrict
			QuestionSerializer serializer;
			@Mocked @NonStrict
			DateUtils dateUtils;
			{
				resultSet.getInt("roomID"); result = 1; times = 1;
				
				resultSet.getString("nickname"); result = "user1"; times = 1;
				resultSet.getString("jid"); result = "user1"; times = 1;
				resultSet.getString("question"); result = "test"; times = 1;
				
				QuestionSerializer.deserialize(anyString); result = new ArrayList<String>();
				
				resultSet.getString("timestamp"); result = "currentDate"; times = 1;
				
				DateUtils.convertTextTimestamp(anyString); result = new Date();
			}
		};
		
		questionLoader.processRow(resultSet);
	}
	
	@Test
	public void processRow_ExceptionOccurs_SkipSetDeskQuestion() throws SQLException
	{
		new Expectations()
		{
			@Mocked @NonStrict Logger logger;
			{
				resultSet.getInt("roomID"); result = new Exception();
			}
		};
		
		questionLoader.processRow(resultSet);
	}
	
	@Test
	public void assignQuestionsToDesk_HaveQuestionForOneDesk_SetQuestionToDesk() throws SQLException
	{
		final List<String> desk1QuestionList = new ArrayList<String>();
		
		new Expectations()
		{
			@Mocked @NonStrict QuestionSerializer serializer;
			@Mocked @NonStrict DateUtils dateUtils;
			{
				resultSet.getInt("roomID"); result = 1;
				
				resultSet.getString("nickname"); result = "user1";
				resultSet.getString("jid"); result = "user1-jid";
				resultSet.getString("question"); result = "test";
				
				QuestionSerializer.deserialize(anyString); result = desk1QuestionList;
				
				resultSet.getString("timestamp"); result = "currentDate";
				
				DateUtils.convertTextTimestamp(anyString); result = new Date();
			}
		};
		
		questionLoader.processRow(resultSet);
		
		Desk desk1 = new Desk("desk1", "virtualdesk");
		desk1.setID(1);

		List<Desk> deskList = new ArrayList<Desk>();
		deskList.add(desk1);
		
		questionLoader.assignQuestionsToDesk(deskList);
		
		Assert.assertEquals("user1", desk1.getQuestion("user1").getPosterNickname());
		Assert.assertEquals("user1-jid", desk1.getQuestion("user1").getPosterJID());
		Assert.assertEquals(desk1QuestionList, desk1.getQuestion("user1").getQuestions());
		Assert.assertEquals(WorkflowState.AwaitResponse, desk1.getQuestion("user1").getState());
	}
	
	@Test
	public void assignQuestionsToDesk_HaveTwoQuestionForOneDesk_SetQuestionToDesk() throws SQLException
	{
		final List<String> desk1QuestionList = new ArrayList<String>();
		
		new Expectations()
		{
			@Mocked @NonStrict QuestionSerializer serializer;
			@Mocked @NonStrict DateUtils dateUtils;
			{
				resultSet.getInt("roomID"); result = 1;
				
				resultSet.getString("nickname"); returns("user1", "user2");
				resultSet.getString("jid"); returns("user1-jid", "user2-jid");
				resultSet.getString("question"); result = "test";
				
				QuestionSerializer.deserialize(anyString); result = desk1QuestionList;
				
				resultSet.getString("timestamp"); result = "currentDate";
				
				DateUtils.convertTextTimestamp(anyString); result = new Date();
			}
		};
		
		questionLoader.processRow(resultSet);
		questionLoader.processRow(resultSet);
		
		Desk desk1 = new Desk("desk1", "virtualdesk");
		desk1.setID(1);
		
		List<Desk> deskList = new ArrayList<Desk>();
		deskList.add(desk1);
		
		questionLoader.assignQuestionsToDesk(deskList);
		
		Assert.assertEquals(2, desk1.getAllQuestions().size());
		Assert.assertEquals("user1", desk1.getQuestion("user1").getPosterNickname());
		Assert.assertEquals("user1-jid", desk1.getQuestion("user1").getPosterJID());
		Assert.assertEquals("user2", desk1.getQuestion("user2").getPosterNickname());
		Assert.assertEquals("user2-jid", desk1.getQuestion("user2").getPosterJID());
	}
	
	@Test
	public void assignQuestionsToDesk_HaveNoQuestionForDesk_SkipSetQuestionToDesk() throws SQLException
	{
		final List<String> desk1QuestionList = new ArrayList<String>();
		
		new Expectations()
		{
			@Mocked @NonStrict QuestionSerializer serializer;
			@Mocked @NonStrict DateUtils dateUtils;
			{
				resultSet.getInt("roomID"); result = 1;
				
				resultSet.getString("nickname"); result = "user1";
				resultSet.getString("jid"); result = "user1";
				resultSet.getString("question"); result = "test";
				
				QuestionSerializer.deserialize(anyString); result = desk1QuestionList;
				
				resultSet.getString("timestamp"); result = "currentDate";
				
				DateUtils.convertTextTimestamp(anyString); result = new Date();
			}
		};
		
		questionLoader.processRow(resultSet);
		
		Desk desk1 = new Desk("desk1", "virtualdesk");
		desk1.setID(2);

		List<Desk> deskList = new ArrayList<Desk>();
		deskList.add(desk1);
		
		questionLoader.assignQuestionsToDesk(deskList);
		
		Assert.assertNull(desk1.getQuestion("user1"));
	}
}

