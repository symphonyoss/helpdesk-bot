package org.symphonyoss.helpdesk.listeners;

import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.ArrayList;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Call implements ChatListener, ChatServiceListener {
    private ArrayList<Member> members = new ArrayList<Member>();
    private HelpClient client;
    private float inactivityTime;
    private BotResponseListener memberListener;
    private HelpClientListener helpClientListener;

    public Call(Member member, HelpClient client, BotResponseListener memberListener, HelpClientListener helpClientListener) {
        members.add(member);
        this.client = client;
        this.memberListener = memberListener;
        this.helpClientListener = helpClientListener;
    }

    public void enterCall() {
        for (Member member : members)
            member.setOnCall(true);
        client.setOnCall(true);

        Chat chat = null;
        chat = helpClientListener.getSymClient().getChatService().getChatByStream(client.getUserID().toString());
        chat.registerListener(this);
        chat.registerListener(helpClientListener);

        for (Member member : members) {
            chat = memberListener.getSymClient().getChatService().getChatByStream(member.getUserID().toString());
            chat.registerListener(this);
            chat.removeListener(memberListener);
        }

        HelpBotConstants.ACTIVECALLS.add(this);
    }

    public void exitCall() {
        for (Member member : members)
            member.setOnCall(false);
        client.setOnCall(false);
        Chat chat = null;

        for (Member member : members) {
            memberListener.getSymClient().getChatService().getChatByStream(member.getUserID().toString());
            chat.removeListener(this);
            chat.registerListener(memberListener);
        }

        HelpBotConstants.ACTIVECALLS.remove(this);
    }

    public void onChatMessage(Message message) {
        if (client.getUserID().equals(message.getFromUserId())) {
            for (Member member : members)
                if (client.getEmail() != null && client.getEmail() != "")
                    Messenger.sendMessage("<messageML><br>" + client.getEmail() + ":</br> " + message.getMessage() + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), memberListener.getSymClient());
                else
                    Messenger.sendMessage("<messageML><br>" + client.getUserID() + ":</br> " + message.getMessage() + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), memberListener.getSymClient());
        } else {
            for (Member m : members) {
                if (!m.getUserID().equals(message.getFromUserId())) {
                    if (MemberDatabase.members.get(message.getFromUserId()).isHideIdentity()) {
                        Messenger.sendMessage("<messageML><br>Member " + (members.indexOf(MemberDatabase.members.get(message.getFromUserId())) + 1)
                                        + ":</br> " + message.getMessage() + "</messageML>",
                                MessageSubmission.FormatEnum.MESSAGEML, m.getUserID(), memberListener.getSymClient());
                    } else {
                        Messenger.sendMessage("<messageML><br>" + client.getEmail() + ":</br> " + message.getMessage()
                                + "</messageML>", MessageSubmission.FormatEnum.MESSAGEML, m.getUserID(), memberListener.getSymClient());
                    }
                }
            }
        }
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
                exitCall();
        } else
            exitCall();

    }

    public HelpClient getClient() {
        return client;
    }

    public void setClient(HelpClient client) {
        this.client = client;
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

    public void setMemberListener(BotResponseListener memberListener) {
        this.memberListener = memberListener;
    }
}
