package org.symphonyoss.helpdesk.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.CallChatListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.listeners.command.CallResponseListener;
import org.symphonyoss.helpdesk.listeners.service.CallServiceListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.*;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Call {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    private final ArrayList<Member> members = new ArrayList<Member>();
    private final ArrayList<HelpClient> clients = new ArrayList<HelpClient>();
    private final AiCommandListener memberListener;
    private final HelpClientListener helpClientListener;

    private SymphonyClient symClient;
    private AiCommandListener callResponseListener;
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

    private void constructCall(){
        callResponder = new CallResponder(this, symClient);
        callChatListener = new CallChatListener(this, callResponseListener, symClient);
        callServiceListener = new CallServiceListener();
        symClient.getChatService().registerListener(callServiceListener);
    }

    public void initiateCall() {
        callResponseListener = new CallResponseListener(symClient, this);

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public void exitCall() {
        for (Member member : new LinkedList<Member>(members))
            exit(member);
        for (HelpClient client : new LinkedList<HelpClient>(clients))
            exit(client);

        symClient.getChatService().removeListener(callServiceListener);
    }

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

        if (clients.size() == 0 && members.size() == 0)
            CallCache.endCall(this);
    }

    public void exit(Member member) {

        Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

        stopListening(chat);
        memberListener.listenOn(chat);

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                MessageSubmission.FormatEnum.TEXT, member.getUserID(), symClient);

        for (HelpClient c : clients)
            callResponder.sendExitMessage(c, member);

        for (Member m : members)
            if (m != member)
                callResponder.sendExitMessage(m, member);


        member.setOnCall(false);
        members.remove(member);

        if (clients.size() == 0 && members.size() == 0)
            CallCache.endCall(this);
    }

    public void stopListening(Chat chat) {
        callChatListener.stopListening(chat);
        callResponseListener.stopListening(chat);
    }

    public void listenOn(Chat chat) {
        callChatListener.listenOn(chat);
        callResponseListener.listenOn(chat);
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
