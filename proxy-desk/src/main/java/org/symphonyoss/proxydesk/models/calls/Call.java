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

package org.symphonyoss.proxydesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.proxydesk.listeners.chat.CallChatListener;
import org.symphonyoss.proxydesk.listeners.service.CallServiceListener;
import org.symphonyoss.proxydesk.models.users.DeskUser;
import org.symphonyoss.proxydesk.utils.CallCache;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by nicktarsillo on 6/14/16.
 * A model that represents a call.
 */
public class Call {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    protected SymphonyClient symClient;
    protected ArrayList<DeskUser> deskUsers = new ArrayList<DeskUser>();
    protected CallResponder callResponder;
    protected CallChatListener callChatListener;
    protected CallServiceListener callServiceListener;
    private boolean privateCall;
    private float inactivityTime;

    public Call(SymphonyClient symClient, boolean privateCall) {
        this.symClient = symClient;
        this.privateCall = privateCall;

        constructCall();
    }

    /**
     * Instantiate the call command listener.
     * Instantiate the call responder.
     * Instantiate the call chat listener.
     * Instantiate the call service listener.
     * Add the service listener to the sym client.
     */
    protected void constructCall() {
        callResponder = new CallResponder(this, symClient);

        callChatListener = new CallChatListener(this, symClient);
        callServiceListener = new CallServiceListener(this);

        symClient.getChatService().registerListener(callServiceListener);
    }

    /**
     * Starts the call.
     */
    public void initiateCall() {

        if (callResponder == null
                || deskUsers == null) {

            if (logger != null)
                logger.error("Call was initiated before being constructed.");

            return;
        }

        if (privateCall) {
            for (DeskUser deskUser : deskUsers) {
                deskUser.setOnCall(true);
                deskUser.setCall(this);
            }
        }


        for (DeskUser deskUser : deskUsers) {
            Chat chat = Messenger.getChat(deskUser.getUserID(), symClient);

            if (privateCall)
                callResponder.sendConnectedMessage(deskUser);

            listenOn(chat);

        }

    }

    /**
     * @param deskUser the desk user trying to enter the chat
     */
    public void enter(DeskUser deskUser) {

        if (deskUsers == null)
            deskUsers = new ArrayList<DeskUser>();

        if (deskUser != null) {

            deskUsers.add(deskUser);
            if (privateCall) {
                deskUser.setOnCall(true);
                deskUser.setCall(this);
            }

            Chat chat = Messenger.getChat(deskUser.getUserID(), symClient);

            listenOn(chat);

        } else {
            if (logger != null)
                logger.error("Client is null. Cannot enter.");
        }


    }

    /**
     * Completely exits the call.
     * Auto exits all users.
     * Removes service listener.
     */
    public void exitCall() {

        if (deskUsers != null) {

            for (DeskUser deskUser : new LinkedList<DeskUser>(deskUsers)) {
                exit(deskUser);
            }

        }

        CallCache.removeCall(this);
        if (symClient != null)
            symClient.getChatService().removeListener(callServiceListener);
    }

    /**
     * @param deskUser the desk user trying to exit the chat
     */
    public void exit(DeskUser deskUser) {

        if (deskUsers.contains(deskUser)) {

            Chat chat = Messenger.getChat(deskUser.getUserID(), symClient);

            stopListening(chat);

            deskUser.setOnCall(false);
            deskUsers.remove(deskUser);

            if (privateCall)
                callResponder.sendLeftCallMessage(deskUser.getUserID());

            if (deskUsers.size() == 0) {
                CallCache.removeCall(this);
                symClient.getChatService().removeListener(callServiceListener);
            }

        } else {
            if (logger != null)
                logger.error("Client {} is not in the call.", deskUser.getUserID());
        }

    }

    /**
     * Removes all call listeners from provided chat.
     *
     * @param chat the chat to remove listeners from
     */
    public void stopListening(Chat chat) {

        if (chat != null) {

            if (callChatListener != null) {

                callChatListener.stopListening(chat);

            } else {
                if (logger != null)
                    logger.error("Could not listen on chat, because a listener was null.");
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

    }

    /**
     * Registers all call listeners from provided chat.
     *
     * @param chat the chat to register listeners to
     */
    public void listenOn(Chat chat) {

        if (chat != null) {

            if (callChatListener != null) {

                callChatListener.listenOn(chat);

            } else {
                if (logger != null)
                    logger.error("Could not listen on chat, because a listener was null.");
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

    }

    public CallTypes getCallType() {
        return CallTypes.BASE_CALL;
    }

    public void logChatError(Chat chat, Exception e) {
        if (logger != null) {

            if (chat == null) {
                logger.error("Ignored method call. Chat was null value.", e);

            } else if (chat.getStream() == null) {
                logger.error("Could not put stream in push hash. " +
                        "Chat stream was null value.", e);

            } else if (chat.getStream().getId() == null) {
                logger.error("Coulot put stream in push hash. " +
                        "Chat stream id was null value.", e);
            }

        }
    }

    public float getInactivityTime() {
        return inactivityTime;
    }

    public void setInactivityTime(float inactivityTime) {
        this.inactivityTime = inactivityTime;
    }

    public CallResponder getCallResponder() {
        return callResponder;
    }

    public void setCallResponder(CallResponder callResponder) {
        this.callResponder = callResponder;
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public boolean isPrivateCall() {
        return privateCall;
    }

    public void setPrivateCall(boolean privateCall) {
        this.privateCall = privateCall;
    }

    public ArrayList<DeskUser> getDeskUsers() {
        return deskUsers;
    }

    public void setDeskUsers(ArrayList<DeskUser> deskUsers) {
        this.deskUsers = deskUsers;
    }

    public enum CallTypes {BASE_CALL, HELP_CALL}

    public String toString(){
        String text = "Call " + CallCache.getCallID(this) + ": [ ";
        for(DeskUser deskUser : deskUsers){
            text += deskUser.getEmail() + ", ";
        }

        return text.substring(0, text.length() - 2) + "]";
    }
}
