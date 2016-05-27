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

import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.serializer.QuestionSerializer;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;

import org.symphonyoss.collaboration.virtualdesk.persistence.mapper.DeskMapper;

public class DeskPersistenceManager
{
	private IServiceConfiguration serviceConfiguration;
	private JdbcTemplate jdbcTemplate;
	private TransactionTemplate txTemplate;

	private DeskIDGenerator idGenerator;

	public DeskPersistenceManager(IServiceConfiguration serviceConfiguration)
	{
		this.serviceConfiguration = serviceConfiguration;
	}

	public void setDeskIDGenerator(DeskIDGenerator idGenerator)
	{
		this.idGenerator = idGenerator;
	}

	public void setDataSource(DataSource dataSource)
	{
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setTransactionManager(PlatformTransactionManager txManager)
	{
		this.txTemplate = new TransactionTemplate(txManager);
		this.txTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
	}

	public Collection <Desk> getDesks(String virtualDeskDomain)
	{
		final String LOAD_ALLDESKS = "SELECT roomID, name, naturalName, subject, description, canDiscoverJID FROM ofMucRoom WHERE serviceID = ?";

		List <Desk> deskList = jdbcTemplate.query(LOAD_ALLDESKS, new Object[]{serviceConfiguration.getServiceID()}, new DeskMapper(virtualDeskDomain));

		final String LOAD_DESKPROP = "SELECT P.roomID, R.name as deskName, P.name, propValue FROM ofMucRoomProp P, ofMucRoom R WHERE P.roomID = R.roomID AND P.serviceID = R.serviceID AND R.serviceID = ?";

		jdbcTemplate.query(LOAD_DESKPROP, new Object[]{serviceConfiguration.getServiceID()}, new DeskPropertiesLoader(deskList));
		
		loadAllDeskQuestions(deskList);

		return deskList;
	}

	public void loadAllDeskQuestions(List <Desk> deskList)
	{
		final String LOAD_ALLQUESTIONS = "SELECT roomID, nickname, jid, question, timestamp FROM vdDeskQuestion WHERE serviceID = ?";
		
		DeskQuestionsLoader questionsLoader = new DeskQuestionsLoader();
		
		jdbcTemplate.query(LOAD_ALLQUESTIONS,  new Object[]{serviceConfiguration.getServiceID()}, questionsLoader);
		
		questionsLoader.assignQuestionsToDesk(deskList);


	}
	
	public void updateDeskSetting(final Desk deskRoom)
	{
		txTemplate.execute(new UpdateDeskSettingCallback(deskRoom, serviceConfiguration, jdbcTemplate, idGenerator));
	}
	
	public void saveDeskQuestion(int deskID, UserState question)
	{
		final String INSERT_UPDATE_QUESTIONS = "INSERT INTO vdDeskQuestion VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE question = ?";
		
		String questionMessages = QuestionSerializer.serialize(question.getQuestions());
		String timestamp = DateUtils.dateToMillis(question.getTimestamp());
		
		jdbcTemplate.update(INSERT_UPDATE_QUESTIONS, serviceConfiguration.getServiceID(), deskID, 
				question.getPosterNickname(), question.getPosterJID(), questionMessages, timestamp, questionMessages);

	}
	
	public void updateDeskQuestion(int deskID, UserState question)
	{
		final String UPDATE_QUESTIONS = "UPDATE vdDeskQuestion SET question = ? WHERE serviceID = ? AND nickname = ?";

		String questionMessages = QuestionSerializer.serialize(question.getQuestions());

		jdbcTemplate.update(UPDATE_QUESTIONS, questionMessages, serviceConfiguration.getServiceID(), question.getPosterNickname());
		

	}
	
	public void deleteDeskQuestion(int deskID, String posterNickname)
	{
		final String DELETE_QUESTIONS = "DELETE FROM vdDeskQuestion WHERE serviceID = ? AND roomID = ? AND nickname = ?";
		
		jdbcTemplate.update(DELETE_QUESTIONS, serviceConfiguration.getServiceID(), deskID, posterNickname);
		

	}
//
//	public boolean isDeskExisted(int deskID)
//	{
//		final String GET_DESKCOUNT = "SELECT count(*) FROM ofMucRoom WHERE roomID = ?";
//		
//		// Counter
//		Counters.increment(CounterItems.DatabaseOperationPerSec);
//
//		return jdbcTemplate.queryForInt(GET_DESKCOUNT, deskID) > 0;
//	}
	
	public void destroyDesk(final Desk deskRoom)
	{
		txTemplate.execute(new DestroyDeskCallback(deskRoom, serviceConfiguration, jdbcTemplate));
		

	}
}
