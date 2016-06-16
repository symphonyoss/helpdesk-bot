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
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallDesk;
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
    private final BotResponseListener memberListener;
    private final HelpClientListener helpClientListener;
    private HelpClient client;
    private float inactivityTime;

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

        try {
            UserIdList list = new UserIdList();
            list.add(client.getUserID());
            Chat chat = memberListener.getSymClient().getChatService().getChatByStream(
                    memberListener.getSymClient().getStreamsClient().getStream(list).getId());
            chat.registerListener(this);
            chat.registerListener(helpClientListener);

            for (Member member : members) {
                list = new UserIdList();
                list.add(member.getUserID());
                chat = memberListener.getSymClient().getChatService().getChatByStream(
                        memberListener.getSymClient().getStreamsClient().getStream(list).getId());

                chat.removeListener(memberListener);
                chat.registerListener(this);

                if (member.isHideIdentity()) {
                    Messenger.sendMessage(MLTypes.START_ML + "You have been connected with a help member. " + "."
                                    + MLTypes.BREAK + MLTypes.BREAK + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(), memberListener.getSymClient());
                } else {
                    Messenger.sendMessage(MLTypes.START_ML + "You have been connected with member " + member.getEmail() + ".<br/> <br/>" + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(), memberListener.getSymClient());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String id = client.getEmail();
        if (id == null || id == "")
            id = client.getUserID().toString();

        for (Member member : members)
            Messenger.sendMessage(MLTypes.START_ML + "You have been connected with user" + id + "."
                            + MLTypes.BREAK + MLTypes.BREAK + client.getHelpSummary() + MLTypes.END_ML,
                    MessageSubmission.FormatEnum.MESSAGEML, member.getEmail(), memberListener.getSymClient());
    }

    public void exitCall() {
        for (Member member : members)
            member.setOnCall(false);
        client.setOnCall(false);

        try {
            UserIdList list = new UserIdList();
            list.add(client.getUserID());
            Chat chat = chat = memberListener.getSymClient().getChatService().getChatByStream(
                    memberListener.getSymClient().getStreamsClient().getStream(list).getId());
            chat.registerListener(helpClientListener);
            chat.removeListener(this);

            for (Member member : members) {
                list = new UserIdList();
                list.add(member.getUserID());
                chat = memberListener.getSymClient().getChatService().getChatByStream(
                        memberListener.getSymClient().getStreamsClient().getStream(list).getId());
                chat.registerListener(this);
                chat.removeListener(memberListener);
                Messenger.sendMessage("You have exited the call.", MessageSubmission.FormatEnum.TEXT, member.getEmail(), memberListener.getSymClient());
            }

            Messenger.sendMessage("You have exited the call.", MessageSubmission.FormatEnum.TEXT, client.getUserID(), memberListener.getSymClient());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        if (client.getUserID().equals(message.getFromUserId())) {
            for (Member member : members)
                if (client.getEmail() != null && client.getEmail() != "")
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getEmail(), memberListener.getSymClient());
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getEmail(), memberListener.getSymClient());
        } else {
            for (Member m : members) {
                if (!m.getUserID().equals(message.getFromUserId())) {
                    if (MemberDatabase.getMember(message).isHideIdentity()) {
                        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD +
                                        "Member " + (members.indexOf(MemberDatabase.getMember(message)) + 1)
                                        + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                                MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), memberListener.getSymClient());
                    } else {
                        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + MemberDatabase.getMember(message).getEmail() + ": " + MLTypes.END_BOLD + text
                                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(),
                                memberListener.getSymClient());
                    }
                }
            }
            if (MemberDatabase.getMember(message).isHideIdentity()) {
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + "Member " + (members.indexOf(MemberDatabase.getMember(message)) + 1)
                                + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(), memberListener.getSymClient());
            } else {
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                        + MemberDatabase.getMember(message).getEmail() + ": " + MLTypes.END_BOLD + text
                        + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, client.getUserID(),
                        memberListener.getSymClient());
            }
            setInactivityTime(0);
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
                CallDesk.endCall(this);
        } else
            CallDesk.endCall(this);
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
}
