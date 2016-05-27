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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtils
{
	private static final char[] zeroArray =
			"0000000000000000000000000000000000000000000000000000000000000000".toCharArray();

	private static DateFormat messageDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
	private static DateFormat historyDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
	
	{
		messageDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		historyDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public static String dateToMillis(Date date)
	{
		return zeroPadString(Long.toString(date.getTime()), 15);
	}

	public static String zeroPadString(String string, int length)
	{
		if (string == null || string.length() > length)
		{
			return string;
		}
		StringBuilder buf = new StringBuilder(length);
		buf.append(zeroArray, 0, length - string.length()).append(string);
		return buf.toString();
	}
	
	public static Date convertTextTimestamp(String textTimestamp)
	{
		return new Date(Long.parseLong(textTimestamp.trim()));
	}
	
	public static String formatMessageTimestamp(Date timestamp)
	{
		return messageDateFormat.format(timestamp);
	}
	
	public static Date parseHistoryTimestamp(String timestamp) throws ParseException
	{
		return historyDateFormat.parse(timestamp);
	}
}
