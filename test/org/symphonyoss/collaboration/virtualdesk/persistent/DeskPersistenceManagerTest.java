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

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.serializer.QuestionSerializer;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskIDGenerator;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPersistenceManager;
import org.symphonyoss.collaboration.virtualdesk.persistence.DestroyDeskCallback;
import org.symphonyoss.collaboration.virtualdesk.persistence.UpdateDeskSettingCallback;
import org.symphonyoss.collaboration.virtualdesk.persistence.mapper.DeskMapper;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DeskPersistenceManagerTest
{
	private DeskPersistenceManager deskPersistenceManager;
	
	private @Mocked @NonStrict
	IServiceConfiguration serviceConfiguration;
	private @Mocked @NonStrict
	DeskIDGenerator idGenerator;
	private @Mocked @NonStrict JdbcTemplate jdbcTemplate;
	private @Mocked @NonStrict PlatformTransactionManager transactionManager;
	private @Mocked @NonStrict TransactionTemplate transactionTemplate;
	
	private @Mocked @NonStrict DataSource dataSource;


	
	@Before
	public void before()
	{
		deskPersistenceManager = new DeskPersistenceManager(serviceConfiguration);
		
		deskPersistenceManager.setDataSource(dataSource);
		deskPersistenceManager.setDeskIDGenerator(idGenerator);
		deskPersistenceManager.setTransactionManager(transactionManager);
	}
	
	@Test
	public void getDesks_AnyConditions_ReturnDeskListFromDatabase()
	{
		new Expectations()
		{
			{
				List<Desk> deskList = new ArrayList<Desk>();
				deskList.add(new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN));
				deskList.add(new Desk("Desk2", TestConst.VIRTUALDESK_DOMAIN));
				
				jdbcTemplate.query(anyString, (Object[])any, (DeskMapper)any); result = deskList; times = 1;
			}
		};
		
		List<Desk> deskList = (List <Desk>)deskPersistenceManager.getDesks(TestConst.VIRTUALDESK_DOMAIN);
		
		Assert.assertEquals(2, deskList.size());
		Assert.assertEquals("Desk1", deskList.get(0).getName());
		Assert.assertEquals("Desk2", deskList.get(1).getName());
	}
//	
//	@Test
//	public void isDeskExisted_DeskDoesNotExist_ReturnFalse()
//	{
//		new Expectations()
//		{
//			{
//				jdbcTemplate.queryForInt(anyString, anyInt); result = 0;
//			}
//		};
//		
//		Assert.assertFalse(deskPersistenceManager.isDeskExisted(3));
//	}
//	
//	@Test
//	public void isDeskExisted_DeskExists_ReturnTrue()
//	{
//		new Expectations()
//		{
//			{
//				jdbcTemplate.queryForInt(anyString, anyInt); result = 1;
//			}
//		};
//		
//		Assert.assertTrue(deskPersistenceManager.isDeskExisted(3));
//	}

	@Test
	public void saveDeskQuestion_AnyCondition_SaveQuestionToDatabase()
	{
		final UserState question = new UserState("user1", "user1", "test");
		
		new Expectations()
		{
			@Mocked
			QuestionSerializer serializer;
			{
				QuestionSerializer.serialize(question.getQuestions()); times = 1;
				
				jdbcTemplate.update(anyString, any, any, any, any, any, any, any); times = 1;
			}
		};
		
		deskPersistenceManager.saveDeskQuestion(1, question);
	}
	
	@Test
	public void updateDeskQuestion_AnyConditions_UpdateQuestionToDatabase()
	{
		final UserState question = new UserState("user1", "user1", "test");
		
		new Expectations()
		{
			@Mocked QuestionSerializer serializer;
			{
				QuestionSerializer.serialize(question.getQuestions()); times = 1;
				
				jdbcTemplate.update(anyString, any, any, any); times = 1;
			}
		};
		
		deskPersistenceManager.updateDeskQuestion(1, question);
	}
	
	@Test
	public void deleteDeskQuestion_AnyConditions_DeleteQuestionkFromDatabase()
	{
		new Expectations()
		{
			{
				jdbcTemplate.update(anyString, any, 1, "user1"); times = 1;
			}
		};
		
		deskPersistenceManager.deleteDeskQuestion(1, "user1");
	}
	
	@Test
	public void updateDeskSetting_AnyConditions_CallUpdateSettingWithinTransaction()
	{
		new Expectations()
		{
			{
				transactionTemplate.execute((UpdateDeskSettingCallback)any); times = 1;
			}
		};
		
		deskPersistenceManager.updateDeskSetting(new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN));
	}
	
	@Test
	public void destroyDesk_AnyConditions_CallDestroyDeskWithinTransaction()
	{
		new Expectations()
		{
			{
				transactionTemplate.execute((DestroyDeskCallback)any); times = 1;
			}
		};
		
		deskPersistenceManager.destroyDesk(new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN));
	}
}
