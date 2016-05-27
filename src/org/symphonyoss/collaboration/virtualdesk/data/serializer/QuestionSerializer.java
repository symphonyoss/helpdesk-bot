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
import org.jivesoftware.util.Base64;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;

public class QuestionSerializer extends Serializer<UserState>
{
	@SuppressWarnings ("unchecked")
	@Override
	public UserState read(Kryo kryo, Input input, Class <UserState> type)
	{
		UserState question = new UserState();
		
		question.setQuestions((List<String>)kryo.readObject(input, ArrayList.class));
		question.setPosterNickname(input.readString());
		question.setPosterJID(input.readString());
		question.setTimestamp(new Date(input.readLong()));
	
		return question;
	}

	@Override
	public void write(Kryo kryo, Output output, UserState question)
	{
		kryo.writeObject(output, question.getQuestions());
		output.writeString(question.getPosterNickname());
		output.writeString(question.getPosterJID());
		output.writeLong(question.getTimestamp().getTime());
	}

	public static String serialize(List<String> questionMessages)
	{
		Kryo kryo = new Kryo();
		kryo.register(ArrayList.class);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Output output = new Output(outputStream);
		
		kryo.writeObject(output, questionMessages);
		output.close();
		
		byte[] serializedBytes = outputStream.toByteArray();
		
		return Base64.encodeBytes(serializedBytes);
	}

	@SuppressWarnings ("unchecked")
	public static List<String> deserialize(String serializedQuestionMessages)
	{
		byte[] decodedSerializedQuestionMessages = Base64.decode(serializedQuestionMessages);
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedSerializedQuestionMessages);
		
		Input input = new Input(inputStream);
		
		Kryo kryo = new Kryo();
		kryo.register(ArrayList.class);

		return (List<String>)kryo.readObject(input, ArrayList.class);
	}
}
