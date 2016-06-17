package org.symphonyoss.helpdesk.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.responses.ExitResponse;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallCash;
import org.symphonyoss.helpdesk.utils.ClientCash;
import org.symphonyoss.helpdesk.utils.MemberCash;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.ArrayList;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Call implements ChatListener, ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    private final ArrayList<Member> members = new ArrayList<Member>();
    private final ArrayList<HelpClient> clients = new ArrayList<HelpClient>();
    private final BotResponseListener memberListener;
    private final HelpClientListener helpClientListener;
    private BotResponseListener botResponseListener;
    private float inactivityTime;

    public Call(Member member, HelpClient client, BotResponseListener memberListener, HelpClientListener helpClientListener) {
        members.add(member);
        clients.add(client);
        this.memberListener = memberListener;
        this.helpClientListener = helpClientListener;
    }

    public void enterCall() {
        botResponseListener = new BotResponseListener(memberListener.getSymClient());
        ExitResponse exitResponse = new ExitResponse("Exit", 0, this);
        botResponseListener.getActiveResponses().add(exitResponse);

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
                Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

                helpClientListener.stopListening(chat);
                listenOn(chat);

                Messenger.sendMessage(MLTypes.START_ML + "Connected to help chat."
                        + MLTypes.BREAK + MLTypes.START_BOLD + "Clients in room: " + MLTypes.END_BOLD + getClientList()
                        + MLTypes.BREAK + MLTypes.START_BOLD + "Members in room: " + MLTypes.END_BOLD + getMemberList()
                        + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(), memberListener.getSymClient());
            }

            for (Member member : members) {
                Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

                memberListener.stopListening(chat);
                listenOn(chat);

                Messenger.sendMessage(MLTypes.START_ML + "Connected to help chat."
                        + MLTypes.BREAK + MLTypes.START_BOLD + "Clients in room: " + MLTypes.END_BOLD + getClientList()
                        + MLTypes.BREAK + MLTypes.START_BOLD + "Members in room: " + MLTypes.END_BOLD + getMemberList()
                        + MLTypes.BREAK + MLTypes.START_BOLD + "Help Summary: " + MLTypes.END_BOLD + MLTypes.BREAK + getHelpList()
                        + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), memberListener.getSymClient());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enter(HelpClient client) {
        clients.add(client);
        client.setOnCall(true);
        client.setCall(this);

        Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

        helpClientListener.stopListening(chat);
        listenOn(chat);

        Messenger.sendMessage(MLTypes.START_ML + "Connected to help chat."
                + MLTypes.BREAK + MLTypes.START_BOLD + "Clients in room: " + MLTypes.END_BOLD + getClientList()
                + MLTypes.BREAK + MLTypes.START_BOLD + "Members in room: " + MLTypes.END_BOLD + getMemberList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(), memberListener.getSymClient());

        for (HelpClient c : clients)
            if (c != client) {
                if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                    Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                            client.getEmail() + MLTypes.END_BOLD + " has entered the chat."
                            , MessageSubmission.FormatEnum.MESSAGEML, c.getUserID(), memberListener.getSymClient());
                else
                    Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                            client.getUserID() + MLTypes.END_BOLD + " has entered the chat."
                            , MessageSubmission.FormatEnum.MESSAGEML, c.getUserID(), memberListener.getSymClient());
            }

        for (Member m : members)
            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                        client.getEmail() + MLTypes.END_BOLD + " has entered the chat."
                        , MessageSubmission.FormatEnum.MESSAGEML, m.getUserID(), memberListener.getSymClient());
            else
                Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                        client.getUserID() + MLTypes.END_BOLD + " has entered the chat."
                        , MessageSubmission.FormatEnum.MESSAGEML, m.getUserID(), memberListener.getSymClient());
    }

    public void enter(Member member) {
        members.add(member);
        member.setOnCall(true);
        member.setCall(this);

        Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

        helpClientListener.stopListening(chat);
        listenOn(chat);

        Messenger.sendMessage(MLTypes.START_ML + "Connected to help chat."
                + MLTypes.BREAK + MLTypes.START_BOLD + "Clients in room: " + MLTypes.END_BOLD + getClientList()
                + MLTypes.BREAK + MLTypes.START_BOLD + "Members in room: " + MLTypes.END_BOLD + getMemberList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), memberListener.getSymClient());

        for (HelpClient c : clients)
            if (!member.isHideIdentity())
                Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                        member.getEmail() + MLTypes.END_BOLD + " has entered the chat."
                        , MessageSubmission.FormatEnum.MESSAGEML, c.getUserID(), memberListener.getSymClient());
            else
                Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                        "Member " + members.indexOf(member) + MLTypes.END_BOLD + " has entered the chat."
                        , MessageSubmission.FormatEnum.MESSAGEML, c.getUserID(), memberListener.getSymClient());


        for (Member m : members)
            if (m != member) {
                if (!member.isHideIdentity())
                    Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                            member.getEmail() + MLTypes.END_BOLD + " has entered the chat."
                            , MessageSubmission.FormatEnum.MESSAGEML, m.getUserID(), memberListener.getSymClient());
                else
                    Messenger.sendMessage(MLTypes.START_ML + "Help client " + MLTypes.START_BOLD +
                            "Member " + members.indexOf(member) + MLTypes.END_BOLD + " has entered the chat."
                            , MessageSubmission.FormatEnum.MESSAGEML, m.getUserID(), memberListener.getSymClient());
            }
    }

    public void exitCall() {
        for (Member member : members)
            exit(member);
        for (HelpClient client : clients)
            exit(client);
    }

    public void exit(HelpClient client) {

        Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

        stopListening(chat);
        helpClientListener.listenOn(chat);
        Messenger.sendMessage("You have exited the call.", MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());

        for (HelpClient c : clients) {
            if (c != client) {
                if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                    Messenger.sendMessage(client.getEmail() + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, c.getUserID(), memberListener.getSymClient());
                else
                    Messenger.sendMessage("User " + client.getUserID().toString() + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, c.getUserID(), memberListener.getSymClient());
            }
        }

        for (Member m : members) {
            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                Messenger.sendMessage(client.getEmail() + " has left the call.",
                        MessageSubmission.FormatEnum.TEXT, m.getUserID(), memberListener.getSymClient());
            else
                Messenger.sendMessage("User " + client.getUserID().toString() + " has left the call.",
                        MessageSubmission.FormatEnum.TEXT, m.getUserID(), memberListener.getSymClient());
        }

        client.setOnCall(false);
        clients.remove(client);

        if(clients.size() == 0 && members.size() == 0)
            CallCash.endCall(this);
    }

    public void exit(Member member) {

        Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

        stopListening(chat);
        memberListener.listenOn(chat);
        Messenger.sendMessage("You have exited the call.", MessageSubmission.FormatEnum.TEXT, member.getEmail(), memberListener.getSymClient());

        for (HelpClient client : clients) {
            if (!member.isHideIdentity())
                Messenger.sendMessage(member.getEmail() + " has left the call.",
                        MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());
            else
                Messenger.sendMessage("Member " + members.indexOf(member) + " has left the call.",
                        MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());
        }

        for (Member m : members) {
            if (m != member) {
                if (!member.isHideIdentity())
                    Messenger.sendMessage(member.getEmail() + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, m.getEmail(), memberListener.getSymClient());
                else
                    Messenger.sendMessage("Member " + members.indexOf(member) + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, m.getEmail(), memberListener.getSymClient());
            }
        }

        member.setOnCall(false);
        members.remove(member);

        if(clients.size() == 0 && members.size() == 0)
            CallCash.endCall(this);
    }

    public void onChatMessage(Message message) {
        if(botResponseListener.isCommand(message))
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
        if (MemberCash.hasMember(message.getFromUserId().toString()))
            member = MemberCash.getMember(message);

        if (member != null)
            relayMemberMessage(member, text);
        else
            relayClientMessage(ClientCash.retrieveClient(message), text);
        setInactivityTime(0);
    }


    private void relayMemberMessage(Member member, String text) {
        for (Member m : members) {
            if (member != m) {
                if (!member.isHideIdentity())
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + member.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), memberListener.getSymClient());
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + "Member " + members.indexOf(member) + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), memberListener.getSymClient());
            }
        }
        for (HelpClient client : clients) {
            if (!member.isHideIdentity())
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + member.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getEmail(), memberListener.getSymClient());
            else
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + "Member " + members.indexOf(member) + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getEmail(), memberListener.getSymClient());
        }
    }

    private void relayClientMessage(HelpClient client, String text) {
        for (Member m : members) {
            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), memberListener.getSymClient());
            else
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), memberListener.getSymClient());
        }
        for (HelpClient c : clients) {
            if (c != client) {
                if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, c.getEmail(), memberListener.getSymClient());
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, c.getEmail(), memberListener.getSymClient());
            }
        }
    }

    public void stopListening(Chat chat) {
        chat.removeListener(this);
        chat.removeListener(botResponseListener);
    }

    public void listenOn(Chat chat) {
        chat.registerListener(this);
        chat.registerListener(botResponseListener);
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
                CallCash.endCall(this);
        } else
            CallCash.endCall(this);
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

    public BotResponseListener getBotResponseListener() {
        return botResponseListener;
    }

    public void setBotResponseListener(BotResponseListener botResponseListener) {
        this.botResponseListener = botResponseListener;
    }
}
