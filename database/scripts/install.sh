#!/bin/bash

##
 #
 # Copyright 2016 The Symphony Software Foundation
 #
 # Licensed to The Symphony Software Foundation (SSF) under one
 # or more contributor license agreements.  See the NOTICE file
 # distributed with this work for additional information
 # regarding copyright ownership.  The ASF licenses this file
 # to you under the Apache License, Version 2.0 (the
 # "License"); you may not use this file except in compliance
 # with the License.  You may obtain a copy of the License at
 #
 #   http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing,
 # software distributed under the License is distributed on an
 # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 # KIND, either express or implied.  See the License for the
 # specific language governing permissions and limitations
 # under the License.
 ##

DB_USER=root
DB_PASS=mysql
DB_PRIMARY_SUFFIX=01a

SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

if [ -f "$SCRIPT_DIR"/db.config ]; then
	. "$SCRIPT_DIR"/db.config
fi

function write-message() {
	echo "$(date -u '+%y-%m-%d %H:%M:%S') [INFO] $1" 
}
function write-error() {
	echo "$(date -u '+%y-%m-%d %H:%M:%S') [ERROR] $1" 
}
function terminate() {
	if [ -e "err.tmp" ]; then
		rm err.tmp
	fi
	
	if (( $1 != 0 )); then
		write-error "return EXIT_CODE $1"
	else
		if (( $LOG_INIT )); then
			write-message "return EXIT_CODE 0"
		fi
	fi
	exit $1
}

if [[ $HOSTNAME == *"$DB_PRIMARY_SUFFIX" ]]; then 
	write-message "First node detected. Installing database"; 
		
	if [ -n "$DB_OVERRIDE_ENGINE" ]; then
		write-message "Override database engine to $DB_OVERRIDE_ENGINE"
		sed s/ndb/$DB_OVERRIDE_ENGINE/g <"$SCRIPT_DIR"/create_virtualdesk_db.sql >"$SCRIPT_DIR"/schema_tmp.sql
		SCHEMA_FILE="$SCRIPT_DIR"/schema_tmp.sql
	else
		SCHEMA_FILE="$SCRIPT_DIR"/create_virtualdesk_db.sql
	fi
	
	write-message "Importing virtualdesk-db schema"
	/usr/bin/mysql -u $DB_USER -p$DB_PASS < "$SCHEMA_FILE"  || { write-error "Found error importing schema"; terminate 6; }
fi

write-message "Creating virtualdesk user"; 
/usr/bin/mysql -u$DB_USER -p$DB_PASS -e "GRANT ALL ON virtualdesk.* TO 'virtualdesk'@'%' IDENTIFIED BY 'C0llab06'" || { write-error "Found error creating user"; terminate 7; }

write-message "Database installation completed"
terminate 0;
