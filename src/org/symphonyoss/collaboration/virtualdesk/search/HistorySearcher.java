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

import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.queryFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;


import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistorySearcher
{
	public static final String CLUSTER_NAME_KEY = "cluster.name";
	public static final int DEFAULT_ELASTIC_SEARCH_PORT = 9300;

	private static Logger Logger = LoggerFactory.getLogger(HistorySearcher.class);
	
	private TransportClient client;
	
	public HistorySearcher(String clusterName, String serverAddresses)
	{
		Settings settings = ImmutableSettings.settingsBuilder().put(CLUSTER_NAME_KEY, clusterName).build();
		
		client = new TransportClient(settings);
		
		InetSocketTransportAddress[] transportAddresses = parseServerAddresses(serverAddresses);
		
		for (InetSocketTransportAddress transportAddress : transportAddresses)
		{
			client.addTransportAddress(transportAddress);
		}
		
		Logger.info("HistorySearcher is configured to cluster {} in address {}", clusterName, serverAddresses);
	}
	
	private InetSocketTransportAddress[] parseServerAddresses(String serverAddresses)
	{
		List<InetSocketTransportAddress> transportAddressList = new ArrayList<InetSocketTransportAddress>();
		
		if (serverAddresses != null && serverAddresses.trim().length() > 0)
		{
			String[] serverAddressList = serverAddresses.split(",");
			
			for (String serverAddress : serverAddressList)
			{
				try
				{
					int serverPort = DEFAULT_ELASTIC_SEARCH_PORT;
					
					String[] networkAddress = serverAddress.split(":");
					
					if (networkAddress.length == 2)
					{
						serverPort = Integer.parseInt(networkAddress[1]);
					}
					
					transportAddressList.add(new InetSocketTransportAddress(networkAddress[0], serverPort));
				}
				catch (RuntimeException e)
				{
					Logger.warn(String.format("Cannot parse {} as server address.",  serverAddress), e);
				}
			}
		}
		
		return transportAddressList.toArray(new InetSocketTransportAddress[transportAddressList.size()]);
	}
	
	public HistorySearchResult search(String userBareJID, String parentDeskName, int offset, int pageSize)
	{
		Logger.info("Building the history search criteria for {} with {} at {}/page size {}",
				new Object[] {userBareJID, parentDeskName, offset, pageSize});
		
		FilterBuilder historyFilter = andFilter(termFilter("to_jid", userBareJID), queryFilter(wildcardQuery("from_jid", String.format("%s_*@virtualdesk*", parentDeskName))));

		SearchRequestBuilder prepareSearch = client.prepareSearch("messages");
		
		prepareSearch.setFrom(offset);
		prepareSearch.setSize(pageSize);
		
		prepareSearch.addSort("time", SortOrder.DESC);
		
		prepareSearch.setTypes("muc");
		
		prepareSearch.setFilter(historyFilter);
		
		SearchResponse response = prepareSearch.execute().actionGet();
		

		
		SearchHits searchHits = response.getHits();
		
		Logger.info("Search history message completed within {} milliseconds", response.getTookInMillis());
		
		HistorySearchResult searchResult = new HistorySearchResult();
		
		searchResult.setTotalResult(searchHits.getTotalHits());
		
		for (SearchHit searchHit : searchHits)
		{
			try
			{
				String fromRes = (String)searchHit.getSource().get("from_res");
				
				String body = (String)searchHit.getSource().get("body");

				Date timestamp = DateUtils.parseHistoryTimestamp((String)searchHit.getSource().get("time"));
				
				String deskBareJID = (String)searchHit.getSource().get("from_jid");
				
				String deskName = extractNode(deskBareJID);
				
				searchResult.addHistoryMessage(new HistoryMessage(fromRes, body, deskName, timestamp));
			}
			catch (ParseException e)
			{
				Logger.warn("Cannot parse the history message because invalid timestamp format.");
			}
		}
		
		return searchResult;
	}
	
	private String extractNode(String bareJID)
	{
		String[] jidParts = bareJID.split("@");
		
		return (jidParts.length < 2) ? "" : jidParts[0];
	}
}
