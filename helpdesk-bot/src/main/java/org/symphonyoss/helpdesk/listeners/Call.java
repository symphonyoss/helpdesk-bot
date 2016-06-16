package org.symphonyoss.helpdesk.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.responses.ExitResponse;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallDesk;
import org.symphonyoss.helpdesk.utils.ClientDatabase;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

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

        for (Member member : members)
            member.setOnCall(true);
        for(HelpClient client: clients)
            client.setOnCall(true);

        try {

            for (HelpClient client : clients) {
                Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

                chat.removeListener(memberListener);
                chat.registerListener(this);
                chat.registerListener(botResponseListener);

                Messenger.sendMessage(MLTypes.START_ML + "Connected to help chat."
                                + MLTypes.BREAK + MLTypes.START_BOLD + "Clients in room: " + MLTypes.END_BOLD + getClientList()
                                  + MLTypes.BREAK + MLTypes.START_BOLD + "Members in room: " + MLTypes.END_BOLD + getMemberList()
                                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(), memberListener.getSymClient());
            }

            for (Member member : members) {
                Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

                chat.removeListener(memberListener);
                chat.registerListener(this);
                chat.registerListener(botResponseListener);

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

    public void exitCall() {
        for (Member member : members)
            exit(member);
        for(HelpClient client: clients)
            exit(client);
    }

    public void exit(HelpClient client){

        Chat chat = Messenger.getChat(client.getUserID(), memberListener.getSymClient());

        chat.removeListener(this);
        chat.removeListener(botResponseListener);
        chat.registerListener(memberListener);
        Messenger.sendMessage("You have exited the call.", MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());

        for (HelpClient c : clients) {
            if(c != client) {
                if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                    Messenger.sendMessage(client.getEmail() + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, c.getUserID(), memberListener.getSymClient());
                else
                    Messenger.sendMessage("User " + client.getUserID().toString() + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, c.getUserID(), memberListener.getSymClient());
            }
        }

        for (Member m : members) {
            if(client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                Messenger.sendMessage(client.getEmail() + " has left the call.",
                        MessageSubmission.FormatEnum.TEXT, m.getUserID(), memberListener.getSymClient());
            else
                Messenger.sendMessage("User " + client.getUserID().toString() + " has left the call.",
                        MessageSubmission.FormatEnum.TEXT, m.getUserID(), memberListener.getSymClient());
        }

        clients.remove(client);
    }

    public void exit(Member member){

        Chat chat = Messenger.getChat(member.getUserID(), memberListener.getSymClient());

        chat.removeListener(this);
        chat.removeListener(botResponseListener);
        chat.registerListener(memberListener);
        Messenger.sendMessage("You have exited the call.", MessageSubmission.FormatEnum.TEXT, member.getEmail(), memberListener.getSymClient());

        for (HelpClient client : clients) {
            if(!member.isHideIdentity())
             Messenger.sendMessage(member.getEmail() + " has left the call.",
                    MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());
            else
                Messenger.sendMessage("Member " + members.indexOf(member) + " has left the call.",
                    MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());
        }

        for (Member m : members) {
            if(m != member) {
                if (!member.isHideIdentity())
                    Messenger.sendMessage(member.getEmail() + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, m.getEmail(), memberListener.getSymClient());
                else
                    Messenger.sendMessage("Member " + members.indexOf(member) + " has left the call.",
                            MessageSubmission.FormatEnum.TEXT, m.getEmail(), memberListener.getSymClient());
            }
        }

        members.remove(member);
    }

    public void onChatMessage(Message message) {
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
        if(MemberDatabase.hasMember(message.getFromUserId().toString()))
            member = MemberDatabase.getMember(message);

        if(member != null)
            relayMemberMessage(member, text);
        else
             relayClientMessage(ClientDatabase.retrieveClient(message), text);
        setInactivityTime(0);
    }


    private void relayMemberMessage(Member member, String text) {
        for (Member m : members) {
            if(member != m) {
                if (member.isHideIdentity())
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
            if (member.isHideIdentity())
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
            if(c != client) {
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


    private String getClientList(){
        String list = "";
        for(HelpClient client: clients)
        if(client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
            list += client.getEmail();
        else
            list += "," + client.getUserID();
        return list.substring(1);
    }


    private String getHelpList(){
        String list = "";
        for(HelpClient client: clients)
            list += client.getHelpSummary();
        return list.substring(1);
    }

    private String getMemberList(){
        String list = "";
        for(Member member: members)
            if(!member.isHideIdentity())
                list += member.getEmail();
            else
                list += "," + member.getUserID();
        return list.substring(1);
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
                CallDesk.endCall(this);
        } else
            CallDesk.endCall(this);
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
