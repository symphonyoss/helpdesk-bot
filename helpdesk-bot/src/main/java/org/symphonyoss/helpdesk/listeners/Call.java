package org.symphonyoss.helpdesk.listeners;

import Constants.HelpBotConstants;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.models.MemberDatabase;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.Stream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public void enterCall(){
        for(Member member: members)
          member.setOnCall(true);
        client.setOnCall(true);

        Chat chat = null;
        chat = helpClientListener.getSymClient().getChatService().getChatByStream(client.getUserID().toString());
        chat.registerListener(this);
        chat.registerListener(helpClientListener);

        for(Member member: members) {
            chat = memberListener.getSymClient().getChatService().getChatByStream(member.getUserID().toString());
            chat.registerListener(this);
            chat.removeListener(memberListener);
        }

        HelpBotConstants.ACTIVECALLS.add(this);
    }

    public void exitCall(){
        for(Member member: members)
            member.setOnCall(false);
        client.setOnCall(false);
        Chat chat = null;

        for(Member member: members) {
            memberListener.getSymClient().getChatService().getChatByStream(member.getUserID().toString());
            chat.removeListener(this);
            chat.registerListener(memberListener);
        }

        HelpBotConstants.ACTIVECALLS.remove(this);
    }

    public void onChatMessage(Message message) {
        if(client.getUserID().equals(message.getFromUserId())){
            for(Member member: members)
                if(client.getEmail() != null && client.getEmail() != "")
                    notifyUser(client.getEmail() + ": " + message.getMessage(), member.getUserID());
                 else
                    notifyUser(client.getUserID() + ": " + message.getMessage(), member.getUserID());
        }else {
            for (Member m : members){
                if (!m.getUserID().equals(message.getFromUserId())) {
                    if (MemberDatabase.members.get(message.getFromUserId()).isHideIdentity()) {
                        notifyUser("Member "+ (members.indexOf(MemberDatabase.members.get(message.getFromUserId()))+1)
                                + ": " + message.getMessage(), m.getUserID());
                    } else {
                        notifyUser(client.getEmail() + ": " + message.getMessage(), m.getUserID());
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
        for(Member m: members)
            if(member.getUserID().equals(chat.getLocalUser().getId()))
                member = m;
        if(member != null) {
            member.setOnCall(false);
            members.remove(member);
            if(members.size() == 0)
                exitCall();
        }else
            exitCall();

    }

    void notifyUser(String message, long userID) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
        userMessage.setMessage(message);
        Stream stream = new Stream();
        stream.setId(""+userID);
        try {
            memberListener.getSymClient().getMessagesClient().sendMessage(stream, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HelpClient getClient() {
        return client;
    }

    public ArrayList<Member> getMembers(){
        return members;
    }

    public void setClient(HelpClient client) {
        this.client = client;
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
