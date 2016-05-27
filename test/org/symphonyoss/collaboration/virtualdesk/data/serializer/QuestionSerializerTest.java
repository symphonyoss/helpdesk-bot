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

package org.symphonyoss.collaboration.virtualdesk.data.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.Assert;
import org.jivesoftware.util.Base64;
import org.junit.Before;
import org.junit.Test;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

public class QuestionSerializerTest
{
	private @Mocked @NonStrict Kryo kryo;
	private @Mocked @NonStrict Input input;
	private @Mocked @NonStrict Output output;
	
	private QuestionSerializer questionSerializer;
	
	@Before
	public void before()
	{
		questionSerializer = new QuestionSerializer();
	}
	
	@SuppressWarnings ({"unchecked", "rawtypes"})
	@Test
	public void read_AnyConditions_ReturnQuestionThatIsDeserialized()
	{
		final Date currentTime = new Date();
		
		final List<String> questionList = new ArrayList<String>();
		questionList.add("test message");
		
		new Expectations()
		{
			{
				kryo.readObject((Input)any, (Class)any); result = questionList;
				
				input.readString(); returns("nickname", "jid");
				
				input.readLong(); result = currentTime.getTime();
			}
		};
		
		UserState question = questionSerializer.read(kryo, input, UserState.class);
		
		Assert.assertEquals(questionList, question.getQuestions());
		Assert.assertEquals("nickname", question.getPosterNickname());
		Assert.assertEquals("jid", question.getPosterJID());
		Assert.assertEquals(currentTime, question.getTimestamp());
		Assert.assertEquals(WorkflowState.AwaitResponse, question.getState());
	}
	
	@Test
	public void write_AnyConditions_SerializeQuestion()
	{
		final Date currentTime = new Date();
		
		final List<String> questionList = new ArrayList<String>();
		questionList.add("test message");
		
		UserState question = new UserState();
		question.setQuestions(questionList);
		question.setPosterNickname("nickname");
		question.setPosterJID("jid");
		question.setTimestamp(currentTime);

		new Expectations()
		{
			{
				kryo.writeObject((Output)any, questionList); times = 1;
				
				output.writeString("nickname"); times = 1;
				
				output.writeString("jid"); times = 1;
				
				output.writeLong(currentTime.getTime()); times = 1;
			}
		};
	
		questionSerializer.write(kryo, output, question);
	}
	
	@Test
	public void serialize_AnyConditions_SerializeQuestionList()
	{
		final List<String> questionList = new ArrayList<String>();
		
		new Expectations()
		{
			@Mocked @NonStrict ByteArrayOutputStream outputStream;
			@Mocked @NonStrict Base64 base64;
			{
				kryo.writeObject((Output)any, any); times = 1;
				
				Base64.encodeBytes((byte[])any); times = 1;
			}
		};
		
		QuestionSerializer.serialize(questionList);
	}
	
	@Test
	public void deserialize_AnyConditions_DeserializeToQuestionList()
	{
		final List<String> questionList = new ArrayList<String>();
		
		new Expectations()
		{
			@Mocked @NonStrict ByteArrayInputStream inputStream;
			@Mocked @NonStrict Base64 base64;
			{
				Base64.decode(anyString); times = 1;
				
				kryo.readObject((Input)any, ArrayList.class); times = 1; result = questionList;
			}
		};
		
		List<String> deserializedQuestionList = QuestionSerializer.deserialize("serialized data");
		
		Assert.assertEquals(questionList, deserializedQuestionList);
	}
}
