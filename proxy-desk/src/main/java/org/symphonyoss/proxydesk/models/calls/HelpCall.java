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
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.proxydesk.listeners.chat.HelpCallChatListener;
import org.symphonyoss.proxydesk.listeners.chat.HelpClientListener;
import org.symphonyoss.proxydesk.listeners.command.CallCommandListener;
import org.symphonyoss.proxydesk.listeners.command.MemberCommandListener;
import org.symphonyoss.proxydesk.listeners.service.CallServiceListener;
import org.symphonyoss.proxydesk.models.HelpBotSession;
import org.symphonyoss.proxydesk.models.users.DeskUser;
import org.symphonyoss.proxydesk.models.users.HelpClient;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.ClientCache;
import org.symphonyoss.proxydesk.utils.MemberCache;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by nicktarsillo on 6/14/16.
 * A model that represents a call.
 */
public class HelpCall extends Call {
    private final Logger logger = LoggerFactory.getLogger(HelpCall.class);

    private MemberCommandListener memberCommandListener;
    private HelpClientListener helpClientListener;
    private CallCommandListener callCommandListener;

    private ArrayList<Member> members = new ArrayList<Member>();
    private ArrayList<HelpClient> clients = new ArrayList<HelpClient>();

    private HelpCallResponder helpCallResponder;

    private float inactivityTime;

    public HelpCall(Member member, HelpClient client, HelpBotSession helpBotSession) {
        super(helpBotSession.getSymphonyClient(), true);

        members.add(member);
        clients.add(client);
        deskUsers.add(member);
        deskUsers.add(client);

        this.helpCallResponder = new HelpCallResponder(this, symClient);
        this.memberCommandListener = helpBotSession.getMemberListener();
        this.helpClientListener = helpBotSession.getHelpClientListener();
    }

    /**
     * Instantiate the call command listener.
     * Instantiate the call responder.
     * Instantiate the call web listener.
     * Instantiate the call service listener.
     * Add the service listener to the sym client.
     */
    @Override
    protected void constructCall() {
        callResponder = new CallResponder(this, symClient);

        callCommandListener = new CallCommandListener(symClient, this);
        callChatListener = new HelpCallChatListener(this, callCommandListener, symClient);
        callServiceListener = new CallServiceListener(this);

        symClient.getChatService().registerListener(callServiceListener);
    }

    /**
     * Starts the call.
     * Sets all members and clients on call.
     * Removes respective listeners from members and clients.
     * Registers this listener to all chats with clients and members
     */
    @Override
    public void initiateCall() {
        super.initiateCall();
        if (helpClientListener == null
                || memberCommandListener == null
                || helpCallResponder == null
                || members == null
                || clients == null) {

            if (logger != null)
                logger.error("Call was initiated before being constructed.");

            return;
        }


        for (HelpClient client : clients) {
            Chat chat = Messenger.getChat(client.getUserID(), symClient);

            helpCallResponder.sendRoomInfo(client.getUserID());

            helpClientListener.stopListening(chat);
        }

        for (Member member : members) {
            Chat chat = Messenger.getChat(member.getUserID(), symClient);

            memberCommandListener.stopListening(chat);

            helpCallResponder.sendRoomInfo(member.getUserID());

            helpCallResponder.sendHelpSummary(member.getUserID());
        }

    }

    @Override
    public void enter(DeskUser deskUser) {
        if (deskUser.getUserType() == DeskUser.DeskUserType.MEMBER) {
            enter(MemberCache.getMember(deskUser.getUserID().toString()));
        } else if (deskUser.getUserType() == DeskUser.DeskUserType.HELP_CLIENT) {
            enter(ClientCache.retrieveClient(deskUser.getUserID().toString()));
        }
    }

    /**
     * Allows a single client to enter this web.
     * Set client on call.
     * Cross listeners.
     *
     * @param client the client trying to enter the web
     */
    public void enter(HelpClient client) {
        super.enter(client);
        if (clients == null)
            clients = new ArrayList<HelpClient>();

        if (client != null) {

            clients.add(client);

            Chat chat = Messenger.getChat(client.getUserID(), symClient);

            helpClientListener.stopListening(chat);

            for (HelpClient c : clients) {

                if (c != client) {
                    helpCallResponder.sendEnteredChatMessage(c, client);
                }

            }

            for (Member m : members) {
                helpCallResponder.sendEnteredChatMessage(m, client);
            }

        } else {
            if (logger != null)
                logger.error("Client is null. Cannot enter.");
        }


    }

    /**
     * Allows a single member to enter this web.
     * Set member on call.
     * Cross listeners.
     *
     * @param member the member trying to enter the web
     */
    public void enter(Member member) {
        super.enter(member);

        if (members == null)
            members = new ArrayList<Member>();

        if (member != null) {

            members.add(member);

            Chat chat = Messenger.getChat(member.getUserID(), symClient);

            helpClientListener.stopListening(chat);

            for (HelpClient c : clients) {
                helpCallResponder.sendEnteredChatMessage(c, member);
            }

            for (Member m : members) {

                if (m != member) {
                    helpCallResponder.sendEnteredChatMessage(m, member);
                }

            }

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
    @Override
    public void exitCall() {
        super.exitCall();

        if (members != null) {

            for (Member member : new LinkedList<Member>(members)) {
                exit(member);
            }

        }

    }

    @Override
    public void exit(DeskUser deskUser) {
        if (deskUser.getUserType() == DeskUser.DeskUserType.MEMBER) {

            exit(MemberCache.getMember(deskUser.getUserID().toString()));

        } else if (deskUser.getUserType() == DeskUser.DeskUserType.HELP_CLIENT) {

            exit(ClientCache.retrieveClient(deskUser.getUserID().toString()));

        }
    }

    /**
     * Set client off call.
     * Cross listeners back.
     *
     * @param client the client trying to exit the web
     */
    public void exit(HelpClient client) {
        super.exit(client);

        if (clients.contains(client)) {

            Chat chat = Messenger.getChat(client.getUserID(), memberCommandListener.getSymClient());

            helpClientListener.listenOn(chat);

            for (HelpClient c : clients) {

                if (c != client) {
                    helpCallResponder.sendExitMessage(c, client);
                }

            }

            for (Member m : members) {
                helpCallResponder.sendExitMessage(m, client);
            }

            clients.remove(client);

        } else {
            if (logger != null)
                logger.error("Client {} is not in the call.", client.getUserID());
        }

    }

    /**
     * Set member off call.
     * Cross listeners back.
     * If members are empty end call.
     *
     * @param member the member trying to exit the web
     */
    public void exit(Member member) {
        super.exit(member);
        if (members == null)
            return;

        if (members.contains(member)) {

            Chat chat = Messenger.getChat(member.getUserID(), memberCommandListener.getSymClient());

            memberCommandListener.listenOn(chat);

            for (HelpClient c : clients) {
                helpCallResponder.sendExitMessage(c, member);
            }

            for (Member m : members) {

                if (m != member) {
                    helpCallResponder.sendExitMessage(m, member);
                }

            }

            members.remove(member);

            if(members.size() == 0) {

                for (HelpClient helpClient : clients) {

                    exit(helpClient);

                }

            }

        } else {
            if (logger != null)
                logger.error("Member {} is not in the call.", member.getUserID());
        }


    }

    /**
     * Removes all call listeners from provided web.
     *
     * @param chat the web to remove listeners from
     */
    @Override
    public void stopListening(Chat chat) {
        super.stopListening(chat);
        if (chat != null) {

            if (callChatListener != null && callCommandListener != null) {

                callCommandListener.stopListening(chat);

            } else {
                if (logger != null)
                    logger.error("Could not listen on web, because a listener was null.");
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

    }

    /**
     * Registers all call listeners from provided web.
     *
     * @param chat the web to register listeners to
     */
    @Override
    public void listenOn(Chat chat) {
        super.listenOn(chat);
        if (chat != null) {

            if (callChatListener != null && callCommandListener != null) {

                callCommandListener.listenOn(chat);

            } else {
                if (logger != null)
                    logger.error("Could not listen on web, because a listener was null.");
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

    }

    public CallTypes getCallType() {
        return CallTypes.HELP_CALL;
    }

    public void logChatError(Chat chat, Exception e) {
        if (logger != null) {

            if (chat == null) {
                logger.error("Ignored method call. Chat was null value.", e);

            } else if (chat.getStream() == null) {
                logger.error("Could not put stream in push hash. " +
                        "Chat stream was null value.", e);

            } else if (chat.getStream().getId() == null) {
                logger.error("Could not put stream in push hash. " +
                        "Chat stream id was null value.", e);
            }

        }
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public ArrayList<HelpClient> getClients() {
        return clients;
    }

    public float getInactivityTime() {
        return inactivityTime;
    }

    public void setInactivityTime(float inactivityTime) {
        this.inactivityTime = inactivityTime;
    }


    public HelpCallResponder getHelpCallResponder() {
        return helpCallResponder;
    }

    public void setHelpCallResponder(HelpCallResponder helpCallResponder) {
        this.helpCallResponder = helpCallResponder;
    }
}
