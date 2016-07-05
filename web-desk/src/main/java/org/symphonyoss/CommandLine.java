/*
 *
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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.symphonyoss;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

public class CommandLine {

	@Parameter(names = "--sslTrustAllHosts", description = "Trust all hosts. NOT TO BE USED IN PRODUCTION!", required = false)
	boolean sslTrustAllHosts = false;

	@Parameter(names = "-c", description = "Configuration File. Defaults to \"./helpbot.properties\"", required = false)
	File configFile = new File("./web-desk/helpbot.properties");

	@DynamicParameter(names = "-P", description = "Property Overrides")
	Map<String, Object> propertyOverrides = new HashMap<>();

}
