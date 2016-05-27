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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import junit.framework.Assert;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class HistorySearcherTest
{
	private HistorySearcher historySearcher;
	
	private @Mocked @NonStrict TransportClient transportClient;
	private @Mocked @NonStrict InetSocketTransportAddress address;

	
	@Before
	public void before()
	{
		historySearcher = new HistorySearcher("tropicsearch", "search01:9300,search02:9300");
	}
	
	@Test
	public void constructor_ServerAddressIsNull_NotAddServerAddressToClient()
	{
		new Expectations()
		{
			{
				new InetSocketTransportAddress(anyString, anyInt); times = 0;
				
				transportClient.addTransportAddress((TransportAddress)any); times = 0;
			}
		};
		
		historySearcher = new HistorySearcher("tropicsearch", null);
	}
	
	@Test
	public void constructor_NoServerAddresses_NotAddServerAddressToClient()
	{
		new Expectations()
		{
			{
				new InetSocketTransportAddress(anyString, anyInt); times = 0;
				
				transportClient.addTransportAddress((TransportAddress)any); times = 0;
			}
		};
		
		historySearcher = new HistorySearcher("tropicsearch", "");
	}
	
	@Test
	public void constructor_AddAddressWithNoPort_AddServerAddressWithDefaultPort()
	{
		new Expectations()
		{
			{
				new InetSocketTransportAddress("search01", 9300); times = 1;
				
				transportClient.addTransportAddress((TransportAddress)any); times = 1;
			}
		};
		
		historySearcher = new HistorySearcher("tropicsearch", "search01");
	}
	
	@Test
	public void constructor_AddAddressWithInvalidDataTypeOfPort_SkipServerAddressThatHasInvalidPort()
	{
		new Expectations()
		{
			{
				new InetSocketTransportAddress(anyString, anyInt); times = 0;
				
				transportClient.addTransportAddress((TransportAddress)any); times = 0;
			}
		};
		
		historySearcher = new HistorySearcher("tropicsearch", "search01:abc");
	}
	
	@Test
	public void constructor_AddAddressWithSpecifiedPort_AddServerAddressWithDefaultPort()
	{
		new Expectations()
		{
			{
				new InetSocketTransportAddress("search01", 9200); times = 1;
				
				transportClient.addTransportAddress((TransportAddress)any); times = 1;
			}
		};
		
		historySearcher = new HistorySearcher("tropicsearch", "search01:9200");
	}
	
	@Test
	public void constructor_AddMultipleAddressesWithSpecifiedPort_AddServerAddressWithDefaultPort()
	{
		new Expectations()
		{
			{
				new InetSocketTransportAddress("search01", 9200); times = 1;
				new InetSocketTransportAddress("search02", 9201); times = 1;
				
				transportClient.addTransportAddress((TransportAddress)any); times = 2;
			}
		};
		
		historySearcher = new HistorySearcher("tropicsearch", "search01:9200,search02:9201");
	}
	
	@Test
	public void search_NoResultFound_ReturnResultWithEmptyHistory() throws InterruptedException, ExecutionException
	{
		new Expectations()
		{
			@Mocked @NonStrict SearchRequestBuilder searchBuilder;
			@Mocked @NonStrict SearchResponse searchResponse;
			@Mocked @NonStrict ListenableActionFuture<SearchResponse> actionFuture;
			@Mocked @NonStrict SearchHits searchHits;
			@Mocked @NonStrict Iterator<SearchHit> searchHitIterator;
			{
				transportClient.prepareSearch(anyString); result = searchBuilder;
				
				searchBuilder.execute(); result = actionFuture;
				
				actionFuture.actionGet(); result = searchResponse;
				
				searchResponse.getHits(); result = searchHits;
				
				searchHits.getTotalHits(); result = 0L;
				searchHits.iterator(); result = searchHitIterator;
				
				searchHitIterator.hasNext(); result = false;
			}
		};
		
		HistorySearchResult searchResult = historySearcher.search("user1", "desl1", 0, 10);
		
		Assert.assertEquals(0, searchResult.getTotalResult());
		Assert.assertEquals(0, searchResult.getHistoryMessages().size());
	}
	
	@Test
	public void search_FoundHistoryMessageWithExpectedData_ReturnResultWithThatHistory() throws InterruptedException, ExecutionException
	{
		new Expectations()
		{
			@Mocked @NonStrict SearchRequestBuilder searchBuilder;
			@Mocked @NonStrict SearchResponse searchResponse;
			@Mocked @NonStrict ListenableActionFuture<SearchResponse> actionFuture;
			@Mocked @NonStrict SearchHits searchHits;
			@Mocked @NonStrict Iterator<SearchHit> searchHitIterator;
			@Mocked @NonStrict SearchHit searchHitItem;
			{
				transportClient.prepareSearch(anyString); result = searchBuilder;
				
				searchBuilder.execute(); result = actionFuture;
				
				actionFuture.actionGet(); result = searchResponse;
				
				searchResponse.getHits(); result = searchHits;
				
				searchHits.getTotalHits(); result = 1L;
				searchHits.iterator(); result = searchHitIterator;
				
				searchHitIterator.hasNext(); returns(true, false);
				searchHitIterator.next(); result = searchHitItem;
				
				Map<String, Object> historyItemMap = new HashMap<String, Object>();
				historyItemMap.put("from_res", "user1");
				historyItemMap.put("body", "test");
				historyItemMap.put("time", "2012-10-01T08:15:01.128");
				historyItemMap.put("from_jid", "desk1@openfire.com/user1");
				
				searchHitItem.getSource(); result = historyItemMap;
			}
		};
		
		HistorySearchResult searchResult = historySearcher.search("user1", "desl1", 0, 10);
		
		Assert.assertEquals(1, searchResult.getTotalResult());
		Assert.assertEquals(1, searchResult.getHistoryMessages().size());
		Assert.assertEquals("desk1", ((List<HistoryMessage>)searchResult.getHistoryMessages()).get(0).getDeskName());
		Assert.assertEquals("user1", ((List<HistoryMessage>)searchResult.getHistoryMessages()).get(0).getFromNickname());
		Assert.assertEquals("test", ((List<HistoryMessage>)searchResult.getHistoryMessages()).get(0).getMessage());
	}
	
	@Test
	public void search_FoundHistoryMessageWithFromJIDIsMalform_ReturnResultWithThatHistoryButDeskIsEmpty() throws InterruptedException, ExecutionException
	{
		new Expectations()
		{
			@Mocked @NonStrict SearchRequestBuilder searchBuilder;
			@Mocked @NonStrict SearchResponse searchResponse;
			@Mocked @NonStrict ListenableActionFuture<SearchResponse> actionFuture;
			@Mocked @NonStrict SearchHits searchHits;
			@Mocked @NonStrict Iterator<SearchHit> searchHitIterator;
			@Mocked @NonStrict SearchHit searchHitItem;
			{
				transportClient.prepareSearch(anyString); result = searchBuilder;
				
				searchBuilder.execute(); result = actionFuture;
				
				actionFuture.actionGet(); result = searchResponse;
				
				searchResponse.getHits(); result = searchHits;
				
				searchHits.getTotalHits(); result = 1L;
				searchHits.iterator(); result = searchHitIterator;
				
				searchHitIterator.hasNext(); returns(true, false);
				searchHitIterator.next(); result = searchHitItem;
				
				Map<String, Object> historyItemMap = new HashMap<String, Object>();
				historyItemMap.put("from_res", "user1");
				historyItemMap.put("body", "test");
				historyItemMap.put("time", "2012-10-01T08:15:01.128");
				historyItemMap.put("from_jid", "desk1"); // JID is malformed
				
				searchHitItem.getSource(); result = historyItemMap;
			}
		};
		
		HistorySearchResult searchResult = historySearcher.search("user1", "desl1", 0, 10);
		
		Assert.assertEquals(1, searchResult.getTotalResult());
		Assert.assertEquals(1, searchResult.getHistoryMessages().size());
		Assert.assertEquals("", ((List<HistoryMessage>)searchResult.getHistoryMessages()).get(0).getDeskName());
		Assert.assertEquals("user1", ((List<HistoryMessage>)searchResult.getHistoryMessages()).get(0).getFromNickname());
		Assert.assertEquals("test", ((List<HistoryMessage>)searchResult.getHistoryMessages()).get(0).getMessage());
	}
	
	@Test
	public void search_FoundHistoryMessageButCannotParseHistoryInfomation_ReturnResultThatSkipFailHistory() throws InterruptedException, ExecutionException, ParseException
	{
		new Expectations()
		{
			@Mocked @NonStrict SearchRequestBuilder searchBuilder;
			@Mocked @NonStrict SearchResponse searchResponse;
			@Mocked @NonStrict ListenableActionFuture<SearchResponse> actionFuture;
			@Mocked @NonStrict SearchHits searchHits;
			@Mocked @NonStrict Iterator<SearchHit> searchHitIterator;
			@Mocked @NonStrict SearchHit searchHitItem;
			@Mocked @NonStrict DateUtils dateUtils;
			{
				transportClient.prepareSearch(anyString); result = searchBuilder;
				
				searchBuilder.execute(); result = actionFuture;
				
				actionFuture.actionGet(); result = searchResponse;
				
				searchResponse.getHits(); result = searchHits;
				
				searchHits.getTotalHits(); result = 1L;
				searchHits.iterator(); result = searchHitIterator;
				
				searchHitIterator.hasNext(); returns(true, false);
				searchHitIterator.next(); result = searchHitItem;
				
				Map<String, Object> historyItemMap = new HashMap<String, Object>();
				historyItemMap.put("from_res", "user1");
				historyItemMap.put("body", "test");
				historyItemMap.put("time", "2012-10-01T08:15:01.128");
				historyItemMap.put("from_jid", "desk1"); // JID is malformed
				
				searchHitItem.getSource(); result = historyItemMap;
				
				DateUtils.parseHistoryTimestamp(anyString); result = new ParseException("", 0);
			}
		};
		
		HistorySearchResult searchResult = historySearcher.search("user1", "desl1", 0, 10);
		
		Assert.assertEquals(1, searchResult.getTotalResult());
		Assert.assertEquals(0, searchResult.getHistoryMessages().size());
	}
}
