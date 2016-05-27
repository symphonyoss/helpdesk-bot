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

%define		_homeDir	/virtualdesk
%define		_scriptDir	/virtualdesk/virtualdesk-db
%define		_db_user	root
%define		_db_pass	mysql
%define		_userhome	%{_scriptDir}
%define		_username	virtualdesk
%define		_usergrp	virtualdesk_grp

Name:		virtualdesk-db
Summary:	Database creation script for Virtual Desk
License:	Eclipse Foundation
Version:	%{buildversion}
Release:	1
Source0:	scripts
Group:		Applications/System
BuildRoot:	%{_builddir}/%{name}-root 
AutoReqProv: no

%description
Database creation script for Virtual Desk

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_scriptDir}
cp -r %{SOURCE0}/* $RPM_BUILD_ROOT%{_scriptDir}

%pre
if [ -z "$(grep '%{_usergrp}': /etc/group)" ]; then groupadd %{_usergrp}; fi
if [ -z "$(grep '%{_username}': /etc/passwd)" ]; then useradd -r -s /bin/bash -c "Virtual Desk Account" -g %{_usergrp} -G users -d $RPM_BUILD_ROOT%{_userhome} %{_username}; fi

%post
echo "Creating database... "
%{_scriptDir}/install.sh
	
%files
%defattr(644, %{_username}, %{_usergrp}, 755)
%{_scriptDir}
%attr(755, %{_username}, %{_usergrp}) %{_scriptDir}/*.sh