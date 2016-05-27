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

package org.symphonyoss.collaboration.virtualdesk.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.serializer.QuestionSerializer;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeskQuestionsLoader implements RowCallbackHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(DeskQuestionsLoader.class);
	
	private Map <Integer, List<UserState>> questionMap;

	public DeskQuestionsLoader()
	{
		questionMap = new HashMap<Integer, List<UserState>>();
	}

	@Override
	public void processRow(ResultSet resultSet) throws SQLException
	{
		try
		{
			int deskID = resultSet.getInt("roomID");
	
			List<UserState> questionList = questionMap.get(deskID);
			
			if (questionList == null)
			{
				questionList = new ArrayList<UserState>();
				
				questionMap.put(Integer.valueOf(deskID), questionList);
			}
			
			UserState question = new  UserState();
			
			question.setPosterNickname(resultSet.getString("nickname"));
			question.setPosterJID(resultSet.getString("jid"));
			
			String serializedQuestionMessages = resultSet.getString("question");
			
			List<String> questionMessages = QuestionSerializer.deserialize(serializedQuestionMessages);
			question.setQuestions(questionMessages);
			
			Date timestamp = DateUtils.convertTextTimestamp(resultSet.getString("timestamp"));
			
			question.setTimestamp(timestamp);
			
			questionList.add(question);
		}
		catch (Throwable e)
		{
			Logger.error("Cannot to load questions", e);
		}
	}
	
	public void assignQuestionsToDesk(List<Desk> deskList)
	{
		for (Desk deskRoom : deskList)
		{
			List<UserState> questionList = questionMap.get(Integer.valueOf(deskRoom.getID()));
			
			if (questionList != null)
			{
				deskRoom.loadQuestions(questionList);
			}
		}
	}
}
