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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.collaboration.virtualdesk.type.Namespace;
import org.xmpp.packet.IQ;

public class DeskSetting
{
	private static Logger Logger = LoggerFactory.getLogger(DeskSetting.class);
	
	private Map <String, List <String>> settingMap;

	public DeskSetting()
	{
		settingMap = new HashMap <String, List <String>>();
	}

	@SuppressWarnings ("unchecked")
	public static DeskSetting parse(IQ iq)
	{
		Logger.debug("Extracting desk setting request packet");
		
		Element queryElement = iq.getChildElement();
		XPath fieldXPath = (XPath) queryElement.createXPath("//x:field");

		Map <String, String> namespaceUris = new HashMap <String, String>();
		namespaceUris.put("x", Namespace.XDATA);

		fieldXPath.setNamespaceURIs(namespaceUris);

		List <Element> fieldElementList = fieldXPath.selectNodes(queryElement);

		DeskSetting setting = new DeskSetting();

		int formTypeCount = 0;

		for (Element fieldElement : fieldElementList)
		{
			String var = fieldElement.attributeValue("var");

			if (var.equalsIgnoreCase("FORM_TYPE"))
			{
				formTypeCount++;
			}

			if (formTypeCount > 1)
			{
				// The second FORM_TYPE is belong to the previous value of setting, so we ignore it
				//  because we care only the new value
				break;
			}

			List <Element> valueElements = fieldElement.elements("value");

			List <String> valueList = new ArrayList <String>();

			for (Element valueElement : valueElements)
			{
				valueList.add(valueElement.getText());
			}

			setting.settingMap.put(var.toLowerCase(), valueList);
		}

		return setting;
	}

	public Map <String, List <String>> getSettings()
	{
		return settingMap;
	}
	
}
