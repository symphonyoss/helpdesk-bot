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

import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPersistenceManager;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DeskDirectoryTest
{
	private DeskDirectory deskDirectory;
	private List<Desk> deskList;
	
	private @Mocked @NonStrict DeskPersistenceManager deskPersistenceManager;

	
	@Before
	public void before()
	{
		deskDirectory = new DeskDirectory(deskPersistenceManager);
		deskDirectory.setDomain(TestConst.VIRTUALDESK_DOMAIN);
		
		deskList = new ArrayList<Desk>();
		deskList.add(new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN));
		deskList.add(new Desk("Desk2", TestConst.VIRTUALDESK_DOMAIN));
	}
	
	@Test
	public void constructor_AnyCondition_NoDeskInDirectory()
	{
		DeskDirectory deskDirectory = new DeskDirectory(TestConst.VIRTUALDESK_DOMAIN, deskPersistenceManager);
		
		Assert.assertEquals(0, deskDirectory.getAllDesks().size());
	}
	
	@Test
	public void loadDesks_AnyConditions_LoadDesksFromDeskPersistenceManager()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.getDesks(anyString); result = deskList;
			}
		};
		
		deskDirectory.loadDesks();
		
		Assert.assertEquals(deskList.size(), deskDirectory.getAllDesks().size());
		Assert.assertTrue(deskDirectory.contains("Desk1"));
		Assert.assertTrue(deskDirectory.contains("Desk2"));
	}
	
	@Test
	public void updateDeskSetting_AnyConditions_CallUpdateDeskSettingOfDeskPersistenceManager()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.updateDeskSetting((Desk)any); times = 1;
			}
		};
		
		deskDirectory.updateDeskSetting(new Desk("Desk3", TestConst.VIRTUALDESK_DOMAIN));
	}
	
	@Test
	public void contains_DeskDoesNotExist_ReturnFalse()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.getDesks(anyString); result = deskList;
			}
		};
		
		deskDirectory.loadDesks();
		
		Assert.assertFalse(deskDirectory.contains("NotExistDesk"));
	}
	
	@Test
	public void contains_DeskExists_ReturnTrue()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.getDesks(anyString); result = deskList;
			}
		};
		
		deskDirectory.loadDesks();
		
		Assert.assertTrue(deskDirectory.contains("Desk1"));
	}
	
	@Test
	public void getDesk_DeskDoesNotExist_ReturnNull()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.getDesks(anyString); result = deskList;
			}
		};
		
		deskDirectory.loadDesks();
		
		Assert.assertNull(deskDirectory.getDesk("NoExistDesk"));
	}
	
	@Test
	public void getDesk_DeskExists_ReturnDeskObject()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.getDesks(anyString); result = deskList;
			}
		};
		
		deskDirectory.loadDesks();
		
		Assert.assertNotNull(deskDirectory.getDesk("Desk1"));
	}
	
	@Test
	public void createDesk_DeskDoesNotInDirectory_ReturnNewCreatedDesk()
	{
		Desk newDesk = deskDirectory.createDesk("NewDesk", "User1");

		Assert.assertEquals("NewDesk", newDesk.getName());
		Assert.assertEquals("User1", newDesk.getCreator());
		Assert.assertFalse(newDesk.canDiscoverJID());
		Assert.assertEquals(-1, newDesk.getID());
	}
	
	@Test
	public void createDesk_DeskHasAlreadyExisted_ReturnExistingDeskWithoutCreatingNew()
	{
		Desk Desk = deskDirectory.createDesk("NewDesk", "User1");
		
		Desk sameDesk = deskDirectory.createDesk("NewDesk", "User1");
		
		Assert.assertSame(Desk, sameDesk);
	}
	
	@Test
	public void getCurrentNumberOfDeskCreated_User1CreatesOneDesk_ReturnOne()
	{
		deskDirectory.createDesk("NewDesk", "User1");
		
		Assert.assertEquals(1, deskDirectory.getCurrentNumberOfDeskCreated("User1"));
	}
	
	@Test
	public void getCurrentNumberOfDeskCreated_User1CreatesTwoDesks_ReturnTwo()
	{
		deskDirectory.createDesk("NewDesk", "User1");
		deskDirectory.createDesk("NewDesk2", "User1");
		
		Assert.assertEquals(2, deskDirectory.getCurrentNumberOfDeskCreated("User1"));
	}
	
	@Test
	public void getCurrentNumberOfDeskCreated_User1CreatesTwoDeskWithSameName_ReturnOne()
	{
		deskDirectory.createDesk("NewDesk", "User1");
		deskDirectory.createDesk("NewDesk", "User1");
		
		Assert.assertEquals(1, deskDirectory.getCurrentNumberOfDeskCreated("User1"));
	}
	
	@Test
	public void getCurrentNumberOfDeskCreated_TwoUsersCreateOneDeskPerEach_ReturnOnePerUser()
	{
		deskDirectory.createDesk("NewDesk", "User1");
		deskDirectory.createDesk("NewDesk2", "User2");
		
		Assert.assertEquals(1, deskDirectory.getCurrentNumberOfDeskCreated("User1"));
		Assert.assertEquals(1, deskDirectory.getCurrentNumberOfDeskCreated("User2"));
	}
	
	@Test
	public void getCurrentNumberOfDeskCreated_GetCreatorWhoNeverCreateDesk_ReturnZero()
	{
		Assert.assertEquals(0, deskDirectory.getCurrentNumberOfDeskCreated("NoCreateDeskUser"));
	}
	
	@Test
	public void destroyDesk_DeskExists()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.getDesks(anyString); result = deskList;
				deskPersistenceManager.destroyDesk((Desk)any); times = 1;
			}
		};
		
		deskDirectory.loadDesks();
		
		deskDirectory.destroyDesk(new Desk("Desk1", TestConst.VIRTUALDESK_DOMAIN));
		
		Assert.assertNull(deskDirectory.getDesk("Desk1"));
		Assert.assertFalse(deskDirectory.contains("Desk1"));
		Assert.assertTrue(deskDirectory.contains("Desk2"));
	}
	
	@Test
	public void destroyDesk_DeskDoesNotExists()
	{
		new Expectations()
		{
			{
				deskPersistenceManager.destroyDesk((Desk)any); times = 1;
			}
		};
		
		Desk desk3 = new Desk("Desk3", TestConst.VIRTUALDESK_DOMAIN);
		desk3.setCreator("owner.user1" + TestConst.VIRTUALDESK_DOMAIN);
		
		deskDirectory.destroyDesk(desk3);
	}
	
	@Test
	public void containsConversation_ConversationDoesNotExist_ReturnFalse()
	{
		Assert.assertFalse(deskDirectory.containsConversation("NotExistedConversation"));
	}
	
	@Test
	public void containsConversation_ConversationExists_ReturnTrue()
	{
		deskDirectory.createDeskConversation("conv1", "a", null);
		
		Assert.assertTrue(deskDirectory.containsConversation("conv1"));
	}
	
	@Test
	public void getConversation_ConversationDoesNotExist_ReturnNull()
	{
		Conversation conversation = deskDirectory.getConversation("NotExistedConversation");
		
		Assert.assertNull(conversation);
	}
	
	@Test
	public void getConversation_ConversationExists_ReturnThatConversation()
	{
		Conversation conversation = deskDirectory.createDeskConversation("conv1", "a", null);
		
		Conversation expectedConversation = deskDirectory.getConversation("conv1");
		
		Assert.assertSame(expectedConversation, conversation);
	}
	
	@Test
	public void createDeskConversation_CreateDuplicateDesk_ReturnSameConversationObject()
	{
		Conversation conversation = deskDirectory.createDeskConversation("conv1", "a", null);
		
		Conversation expectedConversation = deskDirectory.createDeskConversation("conv1", "a", null);
		
		Assert.assertSame(expectedConversation, conversation);
	}
	
	@Test
	public void getAllConversation_HaveConversations_ReturnTheSameNumberOfConversations()
	{
		deskDirectory.createDeskConversation("conv1", "a", null);
		deskDirectory.createDeskConversation("conv2", "b", null);
		
		Assert.assertEquals(2, deskDirectory.getAllConversation().size());
	}

	
	@Test
	public void removeDeskConversation_RemoveExistingConversation_DecreaseNumberOfConversation()
	{
		deskDirectory.createDeskConversation("conv1", "a", null);
		
		deskDirectory.removeDeskConversation("conv1");
		
		Assert.assertEquals(0, deskDirectory.getAllConversation().size());
	}
}
