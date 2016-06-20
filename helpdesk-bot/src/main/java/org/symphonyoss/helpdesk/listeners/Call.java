package org.symphonyoss.helpdesk.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.CallResponseListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallCache;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import sun.security.krb5.internal.crypto.Des;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Call implements ChatListener, ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    private final ArrayList<Member> members = new ArrayList<Member>();
    private final ArrayList<HelpClient> clients = new ArrayList<HelpClient>();
    private final BotResponseListener memberListener;
    private final HelpClientListener helpClientListener;

    private SymphonyClient symClient;
    private BotResponseListener callResponseListener;
    private boolean entered;
    private float inactivityTime;

    public Call(Member member, HelpClient client,
                BotResponseListener memberListener, HelpClientListener helpClientListener,
                SymphonyClient symClient) {
        members.add(member);
        clients.add(client);
        this.memberListener = memberListener;
        this.helpClientListener = helpClientListener;
        this.symClient = symClient;
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

                sendConnectedMessage(client);
            }

            for (Member member : members) {
                Chat chat = Messenger.getChat(member.getUserID(), symClient);

                memberListener.stopListening(chat);
                listenOn(chat);

                sendConnectedMessage(member);
            }
            setEntered(true);
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

        sendConnectedMessage(client);

        for (HelpClient c : clients)
            if (c != client)
                sendEnteredChatMessage(c, client);


        for (Member m : members)
            sendEnteredChatMessage(m, client);
    }

    public void enter(Member member) {
        members.add(member);
        member.setOnCall(true);
        member.setCall(this);

        Chat chat = Messenger.getChat(member.getUserID(), symClient);

        helpClientListener.stopListening(chat);
        listenOn(chat);

        sendConnectedMessage(member);

        for (HelpClient c : clients)
            sendEnteredChatMessage(c, member);

        for (Member m : members)
            if(m != member)
                sendEnteredChatMessage(m, member);
    }

    public void exitCall() {
        for (Member member : new LinkedList<Member>(members))
            exit(member);
        for (HelpClient client : new LinkedList<HelpClient>(clients))
            exit(client);
    }

    public void exit(HelpClient client) {

        Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

        stopListening(chat);
        helpClientListener.listenOn(chat);

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                MessageSubmission.FormatEnum.TEXT, client.getUserID(), symClient);

        for (HelpClient c : clients) {
            if (c != client)
                sendExitMessage(c, client);
        }

        for (Member m : members)
            sendExitMessage(m, client);

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
            sendExitMessage(c, member);

        for (Member m : members)
            if (m != member)
                sendExitMessage(m, member);


        member.setOnCall(false);
        members.remove(member);

        if (clients.size() == 0 && members.size() == 0)
            CallCache.endCall(this);
    }

    public void onChatMessage(Message message) {
        if (callResponseListener.isCommand(message) || !entered)
            return;

        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(memberListener.getSymClient());
            mlMessageParser.parseMessage(message.getMessage());
        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }

        String text = mlMessageParser.getText();
        Member member = null;
        if (MemberCache.hasMember(message.getFromUserId().toString()))
            member = MemberCache.getMember(message);

        if (member != null)
            relayMemberMessage(member, text);
        else
            relayClientMessage(ClientCache.retrieveClient(message), text);
        setInactivityTime(0);
    }

    private void relayMemberMessage(Member member, String text) {
        for (Member m : members) {
            if (member != m) {
                if (!member.isHideIdentity())
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + member.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + HelpBotConstants.MEMBER_LABEL + members.indexOf(member) + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
            }
        }
        for (HelpClient client : clients) {
            if (!member.isHideIdentity())
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + member.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getEmail(), symClient);
            else
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + HelpBotConstants.MEMBER_LABEL + members.indexOf(member) + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getEmail(), symClient);
        }
    }

    private void relayClientMessage(HelpClient client, String text) {
        for (Member m : members) {
            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
            else
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
        }
        for (HelpClient c : clients) {
            if (c != client) {
                if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, c.getEmail(), symClient);
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, c.getEmail(), symClient);
            }
        }
    }

    public void sendRoomInfo(Message message) {
        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.BREAK
                + HelpBotConstants.CLIENTS_LABEL + getClientList()
                + HelpBotConstants.MEMBERS_LABEL + getMemberList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, message, symClient);
    }

    public void sendHelpSummary(Message message) {
        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.BREAK + MLTypes.BREAK + MLTypes.START_BOLD
                + HelpBotConstants.HELP_SUMMARY_LABEL + MLTypes.END_BOLD + MLTypes.BREAK + getHelpList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, message, symClient);
    }

    public void sendEnteredChatMessage(DeskUser user, HelpClient client){
        if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
            Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.HELP_CLIENT_LABEL + MLTypes.START_BOLD +
                    client.getEmail() + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT
                    , MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), memberListener.getSymClient());
        else
            Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.HELP_CLIENT_LABEL + MLTypes.START_BOLD +
                    client.getUserID() + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT
                    , MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), memberListener.getSymClient());
    }

    public void sendEnteredChatMessage(DeskUser user, Member member){
        if (!member.isHideIdentity())
            Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.MEMBER_LABEL + MLTypes.START_BOLD +
                    member.getEmail() + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT + MLTypes.END_ML,
                    MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);
        else
            Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD +
                    HelpBotConstants.MEMBER_LABEL + members.indexOf(member) + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT
                    + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);
    }

    public void sendConnectedMessage(DeskUser user){
        Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.CONNECTED_TO_CALL
                + HelpBotConstants.CLIENTS_LABEL + getClientList()
                + HelpBotConstants.MEMBERS_LABEL + getMemberList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);
    }

    public void sendExitMessage(DeskUser user, HelpClient client){
        if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
            Messenger.sendMessage(client.getEmail() + HelpBotConstants.LEFT_CALL,
                    MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);
        else
            Messenger.sendMessage(HelpBotConstants.HELP_CLIENT_LABEL + client.getUserID().toString() + HelpBotConstants.LEFT_CALL,
                    MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);
    }

    public void sendExitMessage(DeskUser user, Member member){
        if (!member.isHideIdentity())
            Messenger.sendMessage(member.getEmail() + HelpBotConstants.LEFT_CALL,
                    MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);
        else
            Messenger.sendMessage(HelpBotConstants.MEMBER_LABEL + members.indexOf(member) + HelpBotConstants.LEFT_CALL,
                    MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);
    }

    public void stopListening(Chat chat) {
        chat.removeListener(this);
        callResponseListener.stopListening(chat);
    }

    public void listenOn(Chat chat) {
        chat.registerListener(this);
        callResponseListener.listenOn(chat);
    }

    private String getClientList() {
        String list = "";
        for (HelpClient client : clients)
            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                list += client.getEmail();
            else
                list += "," + client.getUserID();
        return list.substring(0);
    }

    private String getHelpList() {
        String list = "";
        for (HelpClient client : clients)
            list += client.getHelpSummary();
        return list;
    }

    private String getMemberList() {
        String list = "";
        for (Member member : members)
            if (!member.isHideIdentity())
                list += member.getEmail();
            else
                list += "," + member.getUserID();
        return list.substring(0);
    }

    public void onNewChat(Chat chat) {
        //Not possible so do nothing
    }

    public void onRemovedChat(Chat chat) {
        Member member = null;
        for (Member m : members)
            if (member.getUserID().equals(chat.getLocalUser().getId()))
                member = m;
        if (member != null) {
            member.setOnCall(false);
            members.remove(member);
            if (members.size() == 0)
                CallCache.endCall(this);
        } else
            CallCache.endCall(this);
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public float getInactivityTime() {
        return inactivityTime;
    }

    public void setInactivityTime(float inactivityTime) {
        this.inactivityTime = inactivityTime;
    }

    public BotResponseListener getMemberListener() {
        return memberListener;
    }

    public BotResponseListener getCallResponseListener() {
        return callResponseListener;
    }

    public void setCallResponseListener(BotResponseListener callResponseListener) {
        this.callResponseListener = callResponseListener;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }
}
