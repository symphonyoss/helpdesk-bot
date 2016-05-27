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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;

public class DateUtilsTest
{
	@Before
	public void before()
	{
		new DateUtils();
	}
	
	@Test
	public void zeroPadString_StringIsNull_ReturnSameString()
	{
		String padString = DateUtils.zeroPadString(null, 5);

		Assert.assertNull(padString);
	}

	@Test
	public void zeroPadString_LengthIsLessThanStringLength_ReturnSameString()
	{
		String padString = DateUtils.zeroPadString("test", 3);

		Assert.assertEquals("test", padString);
	}

	@Test
	public void zeroPadString_LengthIsMoreThanStringLeghth_ReturnSameString()
	{
		String padString = DateUtils.zeroPadString("test", 10);

		Assert.assertEquals(10, padString.length());
		Assert.assertEquals("000000test", padString);
	}

	@Test
	public void dateToMillis_AnyConditions_ReturnTickCountWithPadding()
	{
		Date currentDate = new Date();

		String dateString = Long.toString(currentDate.getTime());

		String padDate = DateUtils.dateToMillis(currentDate);

		Assert.assertEquals(15, padDate.length());
		Assert.assertEquals(dateString, padDate.substring(15 - dateString.length()));
	}
	
	@Test
	public void formatMessageTimestamp_AnyCondition_ReturnTextInMessageTimestampFormat()
	{
		String timeFormat = DateUtils.formatMessageTimestamp(new Date());

		Pattern timePatter = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ");
		Matcher matcher = timePatter.matcher(timeFormat);
		
		Assert.assertTrue(matcher.matches());
	}
	
	@Test
	public void parseHistoryTimestamp_AnyConditions_ReturnDateObjectThatParsedFromText() throws ParseException
	{
		String dateText = "2012-03-05T04:05:30.150";
		
		Date parsedDate = DateUtils.parseHistoryTimestamp(dateText);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(parsedDate);
		
		// Not check the date and hour because the test case will pass/fail depend on
		// time zone of machine that run the test case
		Assert.assertEquals(Calendar.MARCH, calendar.get(Calendar.MONTH));
		Assert.assertEquals(2012, calendar.get(Calendar.YEAR));
		Assert.assertEquals(5, calendar.get(Calendar.MINUTE));
		Assert.assertEquals(30, calendar.get(Calendar.SECOND));
		Assert.assertEquals(150, calendar.get(Calendar.MILLISECOND));
	}
	
	@Test
	public void convertTextTimestamp_AnyConditions_ReturnSameDateThatStoreInText()
	{
		Date currentDate = new Date();
		
		String dateInMillis = DateUtils.dateToMillis(currentDate);
		
		Assert.assertEquals(currentDate, DateUtils.convertTextTimestamp(dateInMillis));
	}
}
