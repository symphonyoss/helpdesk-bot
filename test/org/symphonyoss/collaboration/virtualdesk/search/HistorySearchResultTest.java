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

package org.symphonyoss.collaboration.virtualdesk.search;

import java.util.Date;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class HistorySearchResultTest
{
	private HistorySearchResult searchResult;
	
	@Before
	public void before()
	{
		searchResult = new HistorySearchResult();
	}
	
	@Test
	public void constructor_NewlyCreated_HistoryMessageIsEmpty()
	{
		Assert.assertEquals(0, searchResult.getHistoryMessages().size());	
	}
	
	@Test
	public void addHistoryMessage_AddNewHistory_HistoryMessageSizeIsIncrease()
	{
		searchResult.addHistoryMessage(new HistoryMessage("user1", "test", "desk1", new Date()));
		
		Assert.assertEquals(1, searchResult.getHistoryMessages().size());
	}
	
	@Test
	public void setTotalResult_AnyConditions_CanGetValueBack()
	{
		searchResult.setTotalResult(6);
		
		Assert.assertEquals(6, searchResult.getTotalResult());
	}
}
