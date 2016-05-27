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
-- -----------------------------------------------------
-- Schema `reportdb` 
-- -----------------------------------------------------
CREATE DATABASE IF NOT EXISTS virtualdesk COLLATE utf8_general_ci;

USE virtualdesk;

-- -----------------------------------------------------
-- Table `dim_client`
-- -----------------------------------------------------
# MUC Tables

CREATE TABLE IF NOT EXISTS ofID (
  idType                INTEGER         NOT NULL,
  id                    BIGINT          NOT NULL,
  PRIMARY KEY (idType)
);

CREATE TABLE IF NOT EXISTS ofMucService (
  serviceID           BIGINT        NOT NULL,
  subdomain           VARCHAR(255)  NOT NULL,
  description         VARCHAR(255),
  isHidden            TINYINT       NOT NULL,
  PRIMARY KEY (subdomain),
  INDEX ofMucService_serviceid_idx (serviceID)
);

CREATE TABLE IF NOT EXISTS ofMucServiceProp (
  serviceID           BIGINT        NOT NULL,
  name                VARCHAR(100)  NOT NULL,
  propValue           TEXT          NOT NULL,
  PRIMARY KEY (serviceID, name)
);

CREATE TABLE IF NOT EXISTS ofMucRoom (
  serviceID           BIGINT        NOT NULL,
  roomID              BIGINT        NOT NULL,
  creationDate        CHAR(15)      NOT NULL,
  modificationDate    CHAR(15)      NOT NULL,
  name                VARCHAR(50)   NOT NULL,
  naturalName         VARCHAR(255)  NOT NULL,
  description         VARCHAR(255),
  lockedDate          CHAR(15)      NOT NULL,
  emptyDate           CHAR(15)      NULL,
  canChangeSubject    TINYINT       NOT NULL,
  maxUsers            INTEGER       NOT NULL,
  publicRoom          TINYINT       NOT NULL,
  moderated           TINYINT       NOT NULL,
  membersOnly         TINYINT       NOT NULL,
  canInvite           TINYINT       NOT NULL,
  roomPassword        VARCHAR(50)   NULL,
  canDiscoverJID      TINYINT       NOT NULL,
  logEnabled          TINYINT       NOT NULL,
  subject             VARCHAR(100)  NULL,
  rolesToBroadcast    TINYINT       NOT NULL,
  useReservedNick     TINYINT       NOT NULL,
  canChangeNick       TINYINT       NOT NULL,
  canRegister         TINYINT       NOT NULL,
  PRIMARY KEY (serviceID,name),
  INDEX ofMucRoom_roomid_idx (roomID),
  INDEX ofMucRoom_serviceid_idx (serviceID)
);

CREATE TABLE IF NOT EXISTS ofMucRoomProp (
  serviceID             BIGINT        	NOT NULL,
  roomID                BIGINT          NOT NULL,
  name                  VARCHAR(100)    NOT NULL,
  propValue             TEXT            NOT NULL,
  PRIMARY KEY (serviceID, roomID, name)
);


CREATE TABLE IF NOT EXISTS vdDeskQuestion (
  serviceID           	BIGINT        	NOT NULL,
  roomID                BIGINT          NOT NULL,
  nickname              VARCHAR(255)    NOT NULL,
  jid             		VARCHAR(1024)   NOT NULL,
  question				TEXT			NOT NULL,
  timestamp				CHAR(15)		NOT NULL,
  PRIMARY KEY (serviceID, roomID, nickname)
);

