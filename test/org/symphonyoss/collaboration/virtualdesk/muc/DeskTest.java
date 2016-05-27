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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.persistence.DeskPersistenceManager;
import org.symphonyoss.collaboration.virtualdesk.utils.JIDUtils;
import org.symphonyoss.collaboration.virtualdesk.utils.TestConst;
import org.symphonyoss.collaboration.virtualdesk.utils.UserCreator;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class DeskTest
{
	private Desk deskRoom;
	
	private @Mocked @NonStrict
	DeskPersistenceManager deskPersisteceManager;
	
	@Before
	public void before()
	{
		deskRoom = new Desk("desk1", TestConst.VIRTUALDESK_DOMAIN);
		
		deskRoom.setDeskPersistenceManager(deskPersisteceManager);
	}
	
	@Test
	public void constructor_DeskNameIsNormalText_SetDeskPropertiesToDefault()
	{
		Assert.assertEquals("desk1", deskRoom.getName());
		Assert.assertEquals("desk1", deskRoom.getNaturalName());
		Assert.assertEquals("desk1", deskRoom.getJID().getNode());
		Assert.assertEquals(true, deskRoom.isPersistent());
		Assert.assertEquals(true, deskRoom.isNewlyCreated());
		Assert.assertEquals(0, deskRoom.getCurrentMemberCount());
		Assert.assertEquals(0, deskRoom.getCurrentParticipantCount());
		Assert.assertEquals(0, deskRoom.getOwners().size());
		Assert.assertEquals(0, deskRoom.getAdmins().size());
		Assert.assertEquals(0, deskRoom.getMembers().size());
		Assert.assertEquals(0, deskRoom.getParticipants().size());
	}
	
	@Test
	public void constructor_DeskNameHasSpecialCharacter_SetDeskPropertiesWithUnescapeDeskName()
	{
		deskRoom = new Desk("desk1\\26co", TestConst.VIRTUALDESK_DOMAIN);
		
		Assert.assertEquals("desk1\\26co", deskRoom.getName());
		Assert.assertEquals("desk1&co", deskRoom.getNaturalName());
	}
	
	@Test
	public void setID_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.setID(68);
		
		Assert.assertEquals(68, deskRoom.getID());
	}
	
	@Test
	public void setName_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.setName("newdesk");
		
		Assert.assertEquals("newdesk", deskRoom.getName());
	}
	
	@Test
	public void setNaturalName_NaturalNameHasSpecialCharacters_CanGetTheSameValueBackWithUnescape()
	{
		deskRoom.setNaturalName("new\\20\\22\\26\\27\\2f\\3a\\3c\\3e\\40\\5cdesk");
		
		Assert.assertEquals("new \"&'/:<>@\\desk", deskRoom.getNaturalName());
	}
	
	@Test
	public void setCreator_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.setCreator("abc");
		
		Assert.assertEquals("abc", deskRoom.getCreator());
	}
	
	@Test
	public void setSubject_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.setSubject("new subject");
		
		Assert.assertEquals("new subject", deskRoom.getSubject());
	}
	
	@Test
	public void setDescription_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.setDescription("new description");
		
		Assert.assertEquals("new description", deskRoom.getDescription());
	}
	
	@Test
	public void setCanDiscoverJID_AnyConditions_CanGetTheSameValueBackWithUnescape()
	{
		deskRoom.setCanDiscoverJID(true);
		
		Assert.assertEquals(true, deskRoom.canDiscoverJID());
	}
	
	@Test
	public void isMembersOnly_AnyConditions_CanGetIsMembersOnlyWithSameValue()
	{
		deskRoom.isMembersOnly(true);
		
		Assert.assertEquals(true, deskRoom.isMembersOnly());
	}
	
	@Test
	public void getVirtualDeskUser_AnyConditions_GetTheVirtualUserWithNicknameEqualsDeskName()
	{
		deskRoom.setCanDiscoverJID(true);
		
		Assert.assertEquals("desk1", deskRoom.getVirtualDeskUser().getNickname());
	}
	
	@Test
	public void addParticipant_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.addParticipants("part1");
		
		Assert.assertEquals(1, deskRoom.getParticipants().size());
		Assert.assertTrue(deskRoom.getParticipants().contains("part1"));
	}
	
	@Test
	public void addMember_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.addMember("member1");
		
		Assert.assertEquals(1, deskRoom.getMembers().size());
		Assert.assertTrue(deskRoom.getMembers().contains("member1"));
	}
	
	@Test
	public void addAdmin_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.addAdmin("admin1");
		
		Assert.assertEquals(1, deskRoom.getAdmins().size());
		Assert.assertTrue(deskRoom.getAdmins().contains("admin1"));
	}
	
	@Test
	public void addOwner_AnyConditions_CanGetTheSameValueBack()
	{
		deskRoom.addOwner("owner1");
		
		Assert.assertEquals(1, deskRoom.getOwners().size());
		Assert.assertTrue(deskRoom.getOwners().contains("owner1"));
	}
	
	@Test
	public void isMemberParticipant_UserIsNotInParticipantList_ReturnFalse()
	{
		Assert.assertFalse(deskRoom.isMemberParticipant("part1"));
	}
	
	@Test
	public void isMemberParticipant_UserIsInParticipantList_ReturnTrue()
	{
		deskRoom.addParticipants("part1");
		
		Assert.assertTrue(deskRoom.isMemberParticipant("part1"));
	}
	
	@Test
	public void IsMember_UserIsNotInMemberList_ReturnFalse()
	{
		Assert.assertFalse(deskRoom.isMember("member1"));
	}
	
	@Test
	public void IsMember_UserIsInMemberList_ReturnTrue()
	{
		deskRoom.addMember("member1");
		
		Assert.assertTrue(deskRoom.isMember("member1"));
	}
	
	@Test
	public void IsAdmin_UserIsNotInAdminList_ReturnFalse()
	{
		Assert.assertFalse(deskRoom.isAdmin("admin1"));
	}
	
	@Test
	public void IsAdmin_UserIsInAdminList_ReturnTrue()
	{
		deskRoom.addAdmin("admin1");
		
		Assert.assertTrue(deskRoom.isAdmin("admin1"));
	}
	
	@Test
	public void IsOwner_UserIsNotInOwnerList_ReturnFalse()
	{
		Assert.assertFalse(deskRoom.isOwner("owner1"));
	}
	
	@Test
	public void IsOwner_UserIsInOwnerList_ReturnTrue()
	{
		deskRoom.addOwner("owner1");
		
		Assert.assertTrue(deskRoom.isOwner("owner1"));
	}
	
	@Test
	public void hasDeskMemberPrivilege_UserIsOwner_ReturnTrue()
	{
		deskRoom.addOwner("user1");
		
		Assert.assertTrue(deskRoom.hasDeskMemberPrivilege("user1"));
	}
	
	@Test
	public void hasDeskMemberPrivilege_UserIsAdmin_ReturnTrue()
	{
		deskRoom.addAdmin("user1");
		
		Assert.assertTrue(deskRoom.hasDeskMemberPrivilege("user1"));
	}
	
	@Test
	public void hasDeskMemberPrivilege_UserIsMember_ReturnTrue()
	{
		deskRoom.addMember("user1");
		
		Assert.assertTrue(deskRoom.hasDeskMemberPrivilege("user1"));
	}
	
	@Test
	public void hasDeskMemberPrivilege_UserIsParticipant_ReturnFalse()
	{
		Assert.assertFalse(deskRoom.hasDeskMemberPrivilege("user1"));
	}
	
	@Test
	public void init_AnyConditions_NewlyCreatedMustBeFalse()
	{
		deskRoom.init();
		
		Assert.assertFalse(deskRoom.isNewlyCreated());
	}
	
	@Test
	public void syncParticipants_AnyConditions_ReplaceTheParticipantList()
	{
		Set<String> participantSet = new LinkedHashSet<String>();
		participantSet.add("user1");
		participantSet.add("user2");
		
		deskRoom.syncParticipant(participantSet);
		
		Assert.assertEquals(participantSet.size(), deskRoom.getParticipants().size());
		Assert.assertTrue(deskRoom.getParticipants().contains("user1"));
		Assert.assertTrue(deskRoom.getParticipants().contains("user2"));
	}
	
	@Test
	public void syncMembers_AnyConditions_ReplaceTheMemberList()
	{
		Set<String> memberSet = new LinkedHashSet<String>();
		memberSet.add("user1");
		memberSet.add("user2");
		
		deskRoom.syncMembers(memberSet);
		
		Assert.assertEquals(memberSet.size(), deskRoom.getMembers().size());
		Assert.assertTrue(deskRoom.getMembers().contains("user1"));
		Assert.assertTrue(deskRoom.getMembers().contains("user2"));
	}
	
	@Test
	public void syncAdmins_AnyConditions_ReplaceTheAdminList()
	{
		Set<String> adminSet = new LinkedHashSet<String>();
		adminSet.add("user1");
		adminSet.add("user2");
		
		deskRoom.syncAdmins(adminSet);
		
		Assert.assertEquals(adminSet.size(), deskRoom.getAdmins().size());
		Assert.assertTrue(deskRoom.getAdmins().contains("user1"));
		Assert.assertTrue(deskRoom.getAdmins().contains("user2"));
	}
	
	@Test
	public void syncOwners_AnyConditions_ReplaceTheOwnerList()
	{
		Set<String> ownerSet = new LinkedHashSet<String>();
		ownerSet.add("user1");
		ownerSet.add("user2");
		
		deskRoom.syncOwners(ownerSet);
		
		Assert.assertEquals(ownerSet.size(), deskRoom.getOwners().size());
		Assert.assertTrue(deskRoom.getOwners().contains("user1"));
		Assert.assertTrue(deskRoom.getOwners().contains("user2"));
	}
	
	@Test
	public void addOccupant_UserHasMemberPrivilege_AddToMembers()
	{
		User adminUser = UserCreator.createAdminUser("admin001");
		
		deskRoom.addOccupant(adminUser);
		
		Assert.assertEquals(1, deskRoom.getCurrentMemberCount());
		Assert.assertTrue(deskRoom.getCurrentMembers().contains(adminUser));
	}
	
	@Test
	public void addOccupant_UserIsParticipant_AddToParticipants()
	{
		User participantUser = UserCreator.createParticipantUser("part001");
		
		deskRoom.addOccupant(participantUser);
		
		Assert.assertEquals(1, deskRoom.getCurrentParticipantCount());
		Assert.assertTrue(deskRoom.getCurrentParticipants().contains(participantUser));
	}
	
	@Test
	public void removeOccupant_UserHasMemberPrivilege_RemoveFromMember()
	{
		User adminUser = UserCreator.createAdminUser("admin001");
		
		deskRoom.addOccupant(adminUser);

		deskRoom.removeOccupant(adminUser);

		Assert.assertEquals(0, deskRoom.getCurrentMemberCount());
	}
	
	@Test
	public void removeOccupant_UserIsParticipant_RemoveFromParticipants()
	{
		User participantUser = UserCreator.createParticipantUser("part001");
		
		deskRoom.addOccupant(participantUser);
		
		deskRoom.removeOccupant(participantUser);
		
		Assert.assertEquals(0, deskRoom.getCurrentParticipantCount());
	}
	
	@Test
	public void getOccupantByJID_UserIsInDeskAndHasMemberPrivilege_ReturnThatUser()
	{
		User adminUser = UserCreator.createAdminUser("admin001");
		deskRoom.addOccupant(adminUser);
		
		User getUser = deskRoom.getOccupantByJID(adminUser.getJID());
		
		Assert.assertSame(adminUser, getUser);
	}
	
	@Test
	public void getOccupantByJID_UserIsInDeskAndIsParticipant_ReturnThatUser()
	{
		User participantUser = UserCreator.createParticipantUser("part001");
		deskRoom.addOccupant(participantUser);
		
		User getUser = deskRoom.getOccupantByJID(participantUser.getJID());
		
		Assert.assertSame(participantUser, getUser);
	}
	
	@Test
	public void getOccupantByJID_UserIsNotInDesk_ReturnNull()
	{
		User getUser = deskRoom.getOccupantByJID(JIDUtils.getUserJID("NotInDeskUser"));
		
		Assert.assertNull(getUser);
	}
	
	@Test
	public void getOccupantByNickname_UserIsInDeskAndHasMemberPrivilege_ReturnThatUser()
	{
		User adminUser = UserCreator.createAdminUser("admin001");
		deskRoom.addOccupant(adminUser);
		
		User getUser = deskRoom.getOccupantByNickname(adminUser.getNickname());
		
		Assert.assertSame(adminUser, getUser);
	}
	
	@Test
	public void getOccupantByNickname_UserIsInDeskAndIsParticipant_ReturnThatUser()
	{
		User participantUser = UserCreator.createParticipantUser("part001");
		deskRoom.addOccupant(participantUser);
		
		User getUser = deskRoom.getOccupantByNickname(participantUser.getNickname());
		
		Assert.assertSame(participantUser, getUser);
	}
	
	@Test
	public void getOccupantByNickname_UserIsNotInDesk_ReturnNull()
	{
		User getUser = deskRoom.getOccupantByNickname("NotInDeskUser");
		
		Assert.assertNull(getUser);
	}
	
	@Test
	public void getDeskPresence_NoMemberIsDesk_ReturnExtendedAway()
	{
		PresenceType presence = deskRoom.getDeskPresence();
		
		Assert.assertEquals(PresenceType.ExtendedAway, presence);
	}
	
	@Test
	public void getDeskPresence_AllUsersAreOnline_ReturnOnline()
	{
		User adminUser1 = UserCreator.createAdminUser("admin001");
		User adminUser2 = UserCreator.createAdminUser("admin002");
		
		adminUser1.setPresence(PresenceType.Online);
		adminUser2.setPresence(PresenceType.FreeToChat);
		
		deskRoom.addOccupant(adminUser1);		
		deskRoom.addOccupant(adminUser2);
		
		PresenceType presence = deskRoom.getDeskPresence();
		
		Assert.assertEquals(PresenceType.Online, presence);
	}
	
	@Test
	public void getDeskPresence_SomeUsersAreAway_ReturnOnline()
	{
		User adminUser1 = UserCreator.createAdminUser("admin001");
		User adminUser2 = UserCreator.createAdminUser("admin002");
		
		adminUser1.setPresence(PresenceType.Online);
		adminUser2.setPresence(PresenceType.Away);
		
		deskRoom.addOccupant(adminUser1);		
		deskRoom.addOccupant(adminUser2);
		
		PresenceType presence = deskRoom.getDeskPresence();
		
		Assert.assertEquals(PresenceType.Online, presence);
	}
	
	@Test
	public void getDeskPresence_AllUsersAreAway_ReturnOnline()
	{
		User adminUser1 = UserCreator.createAdminUser("admin001");
		User adminUser2 = UserCreator.createAdminUser("admin002");
		
		adminUser1.setPresence(PresenceType.Away);
		adminUser2.setPresence(PresenceType.Away);
		
		deskRoom.addOccupant(adminUser1);		
		deskRoom.addOccupant(adminUser2);
		
		PresenceType presence = deskRoom.getDeskPresence();
		
		Assert.assertEquals(PresenceType.Away, presence);
	}
	
	@Test
	public void getUserState_UserHasNotPostQuestionYet_ReturnNull()
	{
		Assert.assertNull(deskRoom.getQuestion("abc"));
	}
	
	@Test
	public void getUserState_UserPostNewQuestionAlready_ReturnUserState()
	{
		deskRoom.addNewQuestion("abc", JIDUtils.getUserBareJID("user1"), "This is new question");
		
		UserState userState = deskRoom.getQuestion("abc");
		
		Assert.assertEquals(WorkflowState.AwaitResponse, userState.getState());
		Assert.assertEquals(1, userState.getQuestions().size());
	}
	
	@Test
	public void setDeskAliasName_AnyConditions_CanGetSameNewDeskAlias()
	{
		deskRoom.setDeskAliasName("ABCD");
		
		Assert.assertEquals("ABCD", deskRoom.getDeskAliasName());
	}
	
	@Test
	public void isNicknameExisted_NewNicknameIsUsedByVirtualDeskUser_ReturnTrue()
	{
		boolean isExisted = deskRoom.isNicknameExisted(deskRoom.getVirtualDeskUser().getNickname());
		
		Assert.assertTrue(isExisted);
	}
	
	@Test
	public void isNicknameExisted_NewNicknameIsUsedByMemberUser_ReturnTrue()
	{
		User member = UserCreator.createAdminUser("member001");
		deskRoom.addOccupant(member);
		
		boolean isExisted = deskRoom.isNicknameExisted(member.getNickname());
		
		Assert.assertTrue(isExisted);
	}
	
	@Test
	public void isNicknameExisted_NewNicknameIsUsedByParticipantUser_ReturnTrue()
	{
		User member = UserCreator.createParticipantUser("member001");
		deskRoom.addOccupant(member);
		
		boolean isExisted = deskRoom.isNicknameExisted(member.getNickname());
		
		Assert.assertTrue(isExisted);
	}
	
	@Test
	public void isNicknameExisted_NewNicknameHasNotBeenUsed_ReturnFalse()
	{
		boolean isExisted = deskRoom.isNicknameExisted("NotUsedNickname");
		
		Assert.assertFalse(isExisted);
	}
	
	@Test
	public void getAllQuestions_AddQuestions_GetQuestionItemsFromAdded()
	{
		deskRoom.addNewQuestion("a", "a", "test_a");
		deskRoom.addNewQuestion("b", "b", "test_b");
		
		Collection<UserState> questions = deskRoom.getAllQuestions();
		
		Assert.assertEquals(2, questions.size());
	}
	
	@Test
	public void loadQuestions_loadQuestions_CanGetSameNumberOfItemFromLoadedQuestions()
	{
		List<UserState> questionList = new ArrayList<UserState>();
		
		questionList.add(new UserState("a", "a", "test_a"));
		questionList.add(new UserState("b", "b", "test_b"));
		questionList.add(new UserState("c", "c", "test_c"));
		
		deskRoom.loadQuestions(questionList);
		
		Assert.assertEquals(3, deskRoom.getAllQuestions().size());
	}
	
	@Test
	public void resetQuestionState_PosterDoesNotExist_DoNothing()
	{
		new Expectations()
		{
			@Mocked @NonStrict UserState question;
			{
				question.setState((WorkflowState)any); times = 0;
			}
		};
		
		deskRoom.resetQuestionState("NotExistedUser");
	}
	
	@Test
	public void resetQuestionState_PosterExists_SetWorkflowStateToAwaitResponse()
	{
		deskRoom.addNewQuestion("a", "a", "test");
		UserState question = deskRoom.getQuestion("a");
		
		question.setState(WorkflowState.InConversation);
		
		deskRoom.resetQuestionState("a");
		
		Assert.assertEquals(WorkflowState.AwaitResponse, question.getState());
	}
	
	@Test
	public void updateQuestion_PosterDoesNotExist_DoNothing()
	{
		new Expectations()
		{
			{
				deskPersisteceManager.updateDeskQuestion(anyInt, (UserState)any); times = 0;
			}
		};
		
		deskRoom.updateQuestion("NotExistedUser");
	}
	
	@Test
	public void updateQuestion_PosterExists_UpdateQuestionToDatabase()
	{
		deskRoom.addNewQuestion("a", "a", "test");
		
		new Expectations()
		{
			{
				deskPersisteceManager.updateDeskQuestion(anyInt, (UserState)any); times = 1;
			}
		};
		
		deskRoom.updateQuestion("a");
	}
	
	@Test
	public void closeQuestion_AnyConditions_DeleteQuestionInDatabase()
	{
		
		new Expectations()
		{
			{
				deskPersisteceManager.deleteDeskQuestion(anyInt, "a"); times = 1;
			}
		};
		
		deskRoom.closeQuestion("a");
	}
}
