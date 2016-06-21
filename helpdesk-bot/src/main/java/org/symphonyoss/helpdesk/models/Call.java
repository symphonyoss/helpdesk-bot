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
    private final ArrayList<Member> members = new ArrayList<Member>();
    private final ArrayList<HelpClient> clients = new ArrayList<HelpClient>();
    private final AiCommandListener memberListener;
    private final HelpClientListener helpClientListener;

    private SymphonyClient symClient;
    private AiCommandListener callCommandListener;
    private CallResponder callResponder;
    private CallChatListener callChatListener;
    private CallServiceListener callServiceListener;
    private float inactivityTime;

    public Call(Member member, HelpClient client,
                AiCommandListener memberListener, HelpClientListener helpClientListener,
                SymphonyClient symClient) {
        members.add(member);
        clients.add(client);
        this.memberListener = memberListener;
        this.helpClientListener = helpClientListener;
        this.symClient = symClient;

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
        callCommandListener = new CallCommandListener(symClient, this);
        callResponder = new CallResponder(this, symClient);
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

        for (Member member : members) {
            member.setOnCall(true);
            member.setCall(this);
        }

        for (HelpClient client : clients) {
            client.setOnCall(true);
            client.setCall(this);
        }

        try {
            for (HelpClient client : clients) {
                Chat chat = Messenger.getChat(client.getUserID(), symClient);

                helpClientListener.stopListening(chat);
                listenOn(chat);

                callResponder.sendConnectedMessage(client);
            }

            for (Member member : members) {
                Chat chat = Messenger.getChat(member.getUserID(), symClient);

                memberListener.stopListening(chat);
                listenOn(chat);

                callResponder.sendConnectedMessage(member);
                callResponder.sendHelpSummary(member.getUserID());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Allows a single client to enter this chat.
     * Set client on call.
     * Cross listeners.
     * @param client  the client trying to enter the chat
     */
    public void enter(HelpClient client) {
        clients.add(client);
        client.setOnCall(true);
        client.setCall(this);

        Chat chat = Messenger.getChat(client.getUserID(), symClient);

        helpClientListener.stopListening(chat);
        listenOn(chat);

        callResponder.sendConnectedMessage(client);

        for (HelpClient c : clients)
            if (c != client)
                callResponder.sendEnteredChatMessage(c, client);


        for (Member m : members)
            callResponder.sendEnteredChatMessage(m, client);
    }

    /**
     * Allows a single member to enter this chat.
     * Set member on call.
     * Cross listeners.
     * @param member   the member trying to enter the chat
     */
    public void enter(Member member) {
        members.add(member);
        member.setOnCall(true);
        member.setCall(this);

        Chat chat = Messenger.getChat(member.getUserID(), symClient);

        helpClientListener.stopListening(chat);
        listenOn(chat);

        callResponder.sendConnectedMessage(member);

        for (HelpClient c : clients)
            callResponder.sendEnteredChatMessage(c, member);

        for (Member m : members)
            if (m != member)
                callResponder.sendEnteredChatMessage(m, member);
    }

    /**
     * Completely exits the call.
     * Auto exits all users.
     * Removes service listener.
     */
    public void exitCall() {
        for (Member member : new LinkedList<Member>(members))
            exit(member);
        for (HelpClient client : new LinkedList<HelpClient>(clients))
            exit(client);

        symClient.getChatService().removeListener(callServiceListener);
    }

    /**
     * Set client off call.
     * Cross listeners back.
     * @param client   the client trying to exit the chat
     */
    public void exit(HelpClient client) {

        Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

        stopListening(chat);
        helpClientListener.listenOn(chat);

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                MessageSubmission.FormatEnum.TEXT, client.getUserID(), symClient);

        for (HelpClient c : clients) {
            if (c != client)
                callResponder.sendExitMessage(c, client);
        }

        for (Member m : members)
            callResponder.sendExitMessage(m, client);

        client.setOnCall(false);
        clients.remove(client);

        if (clients.size() == 0 && members.size() == 0) {
            CallCache.endCall(this);
            symClient.getChatService().registerListener(callServiceListener);
        }
    }

    /**
     * Set member off call.
     * Cross listeners back.
     * @param member   the member trying to exit the chat
     */
    public void exit(Member member) {

        Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

        memberListener.setPushCommands(false);
        stopListening(chat);
        memberListener.listenOn(chat);
        memberListener.setPushCommands(true);

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                MessageSubmission.FormatEnum.TEXT, member.getUserID(), symClient);

        for (HelpClient c : clients)
            callResponder.sendExitMessage(c, member);

        for (Member m : members)
            if (m != member)
                callResponder.sendExitMessage(m, member);


        member.setOnCall(false);
        members.remove(member);

        if (clients.size() == 0 && members.size() == 0) {
            CallCache.endCall(this);
            symClient.getChatService().registerListener(callServiceListener);
        }
    }

    /**
     * Removes all call listeners from provided chat.
     *
     * @param chat   the chat to remove listeners from
     */
    public void stopListening(Chat chat) {
        callChatListener.stopListening(chat);
        callCommandListener.stopListening(chat);
    }

    /**
     * Registers all call listeners from provided chat.
     *
     * @param chat   the chat to register listeners to
     */
    public void listenOn(Chat chat) {
        callChatListener.listenOn(chat);
        callCommandListener.listenOn(chat);
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

}
