package org.symphonyoss.helpdesk.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.CallChatListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.listeners.command.CallCommandListener;
import org.symphonyoss.helpdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.helpdesk.listeners.service.CallServiceListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by nicktarsillo on 6/14/16.
 * A model that represents a call.
 */
public class Call {
    private final Logger logger = LoggerFactory.getLogger(Call.class);

    private MemberCommandListener memberCommandListener;
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;

    private ArrayList<Member> members = new ArrayList<Member>();
    private ArrayList<HelpClient> clients = new ArrayList<HelpClient>();

    private AiCommandListener callCommandListener;
    private CallResponder callResponder;
    private CallChatListener callChatListener;
    private CallServiceListener callServiceListener;

    private float inactivityTime;

    public Call(Member member, HelpClient client, HelpBotSession helpBotSession) {
        members.add(member);
        clients.add(client);
        this.memberCommandListener = helpBotSession.getMemberListener();
        this.helpClientListener = helpBotSession.getHelpClientListener();
        this.symClient = helpBotSession.getSymphonyClient();

        constructCall();
    }

    /**
     * Instantiate the call command listener.
     * Instantiate the call responder.
     * Instantiate the call chat listener.
     * Instantiate the call service listener.
     * Add the service listener to the sym client.
     */
    private void constructCall() {
        callResponder = new CallResponder(this, symClient);

        callCommandListener = new CallCommandListener(symClient, this);
        callChatListener = new CallChatListener(this, callCommandListener, symClient);
        callServiceListener = new CallServiceListener(this);

        symClient.getChatService().registerListener(callServiceListener);
    }

    /**
     * Starts the call.
     * Sets all members and clients on call.
     * Removes respective listeners from members and clients.
     * Registers this listener to all chats with clients and members
     */
    public void initiateCall() {

        if (helpClientListener == null
                || memberCommandListener == null
                || callResponder == null
                || members == null
                || clients == null) {

            if (logger != null)
                logger.error("Call was initiated before being constructed.");

            return;
        }

        for (Member member : members) {
            member.setOnCall(true);
            member.setCall(this);
        }

        for (HelpClient client : clients) {
            client.setOnCall(true);
            client.setCall(this);
        }


        for (HelpClient client : clients) {
            Chat chat = Messenger.getChat(client.getUserID(), symClient);

            helpClientListener.stopListening(chat);
            listenOn(chat);

            callResponder.sendConnectedMessage(client);
        }

        for (Member member : members) {
            Chat chat = Messenger.getChat(member.getUserID(), symClient);

            memberCommandListener.stopListening(chat);
            listenOn(chat);

            callResponder.sendConnectedMessage(member);
            callResponder.sendHelpSummary(member.getUserID());
        }

    }

    /**
     * Allows a single client to enter this chat.
     * Set client on call.
     * Cross listeners.
     *
     * @param client the client trying to enter the chat
     */
    public void enter(HelpClient client) {

        if(clients == null)
            clients = new ArrayList<HelpClient>();

        if (client != null) {

            clients.add(client);
            client.setOnCall(true);
            client.setCall(this);

            Chat chat = Messenger.getChat(client.getUserID(), symClient);

            helpClientListener.stopListening(chat);
            listenOn(chat);

            callResponder.sendConnectedMessage(client);

            for (HelpClient c : clients) {

                if (c != client) {
                    callResponder.sendEnteredChatMessage(c, client);
                }

            }

            for (Member m : members) {
                callResponder.sendEnteredChatMessage(m, client);
            }

        } else {
            if (logger != null)
                logger.error("Client is null. Cannot enter.");
        }


    }

    /**
     * Allows a single member to enter this chat.
     * Set member on call.
     * Cross listeners.
     *
     * @param member the member trying to enter the chat
     */
    public void enter(Member member) {

        if(members == null)
            members = new ArrayList<Member>();

        if (member != null) {

            members.add(member);
            member.setOnCall(true);
            member.setCall(this);

            Chat chat = Messenger.getChat(member.getUserID(), symClient);

            helpClientListener.stopListening(chat);
            listenOn(chat);

            callResponder.sendConnectedMessage(member);

            for (HelpClient c : clients) {
                callResponder.sendEnteredChatMessage(c, member);
            }

            for (Member m : members) {

                if (m != member) {
                    callResponder.sendEnteredChatMessage(m, member);
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
    public void exitCall() {

        if(members != null) {

            for (Member member : new LinkedList<Member>(members)) {
                exit(member);
            }

        }

        if(clients == null){

            CallCache.removeCall(this);
            if (symClient != null)
                symClient.getChatService().removeListener(callServiceListener);

            return;
        }

        for (HelpClient client : new LinkedList<HelpClient>(clients)) {
            exit(client);
        }

        if (symClient != null)
            symClient.getChatService().removeListener(callServiceListener);
    }

    /**
     * Set client off call.
     * Cross listeners back.
     *
     * @param client the client trying to exit the chat
     */
    public void exit(HelpClient client) {

        if (clients.contains(client)) {

            Chat chat = Messenger.getChat(client.getUserID(), memberCommandListener.getSymClient());

            stopListening(chat);
            helpClientListener.listenOn(chat);

            Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                    MessageSubmission.FormatEnum.TEXT, client.getUserID(), symClient);

            for (HelpClient c : clients) {

                if (c != client) {
                    callResponder.sendExitMessage(c, client);
                }

            }

            for (Member m : members) {
                callResponder.sendExitMessage(m, client);
            }

            client.setOnCall(false);
            clients.remove(client);

            if (clients.size() == 0 && members.size() == 0) {
                CallCache.removeCall(this);
                symClient.getChatService().removeListener(callServiceListener);
            }

        } else {
            if (logger != null)
                logger.error("Client {} is not in the call.", client.getUserID());
        }

    }

    /**
     * Set member off call.
     * Cross listeners back.
     *
     * @param member the member trying to exit the chat
     */
    public void exit(Member member) {

        if(members == null)
            return;

        if (members.contains(member)) {

            Chat chat = Messenger.getChat(member.getUserID(), memberCommandListener.getSymClient());

            memberCommandListener.setPushCommands(false);

            stopListening(chat);
            memberCommandListener.listenOn(chat);

            memberCommandListener.setPushCommands(true);

            Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                    MessageSubmission.FormatEnum.TEXT, member.getUserID(), symClient);

            for (HelpClient c : clients) {
                callResponder.sendExitMessage(c, member);
            }

            for (Member m : members) {

                if (m != member) {
                    callResponder.sendExitMessage(m, member);
                }

            }


            member.setOnCall(false);
            members.remove(member);

            if (clients.size() == 0 && members.size() == 0) {
                CallCache.removeCall(this);
                symClient.getChatService().removeListener(callServiceListener);
            }

        } else {
            if (logger != null)
                logger.error("Member {} is not in the call.", member.getUserID());
        }


    }

    /**
     * Removes all call listeners from provided chat.
     *
     * @param chat the chat to remove listeners from
     */
    public void stopListening(Chat chat) {

        if (chat != null) {

            if (callChatListener != null && callCommandListener != null) {

                callChatListener.stopListening(chat);
                callCommandListener.stopListening(chat);

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

            if (callChatListener != null && callCommandListener != null) {

                callChatListener.listenOn(chat);
                callCommandListener.listenOn(chat);

            } else {
                if (logger != null)
                    logger.error("Could not listen on chat, because a listener was null.");
            }

        } else {
            logChatError(chat, new NullPointerException());
        }

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
}
