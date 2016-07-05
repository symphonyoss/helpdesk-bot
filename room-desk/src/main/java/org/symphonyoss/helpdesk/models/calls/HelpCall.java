package org.symphonyoss.helpdesk.models.calls;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.listeners.command.HelpCallCommandListener;
import org.symphonyoss.helpdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.helpdesk.models.HelpBotSession;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 7/5/16.
 */
public class HelpCall extends Call {
    private SymphonyClient symClient;

    private HelpCallCommandListener helpCallCommandListener;

    private MemberCommandListener memberCommandListener;
    private HelpClientListener helpClientListener;

    private Chat helpChat;

    private Member member;
    private HelpClient client;

    public HelpCall(Member member, HelpClient client, HelpBotSession session){
        super();
        this.member = member;
        this.client = client;
        this.symClient = session.getSymphonyClient();

        this.memberCommandListener = session.getMemberListener();
        this.helpClientListener = session.getHelpClientListener();
    }

    public void initiateCall() {
        helpChat = new Chat();
        helpChat.setLocalUser(symClient.getLocalUser());

        Set<User> users = new HashSet<User>();

        try {

            users.add(symClient.getUsersClient().getUserFromId(client.getUserID()));
            users.add(symClient.getUsersClient().getUserFromId(member.getUserID()));

            helpChat.setRemoteUsers(users);
            helpChat.setStream(symClient.getStreamsClient().getStream(users));

            helpClientListener.stopListening(getUserChat(client.getUserID()));
            memberCommandListener.stopListening(getUserChat(member.getUserID()));

            helpCallCommandListener = new HelpCallCommandListener(symClient, this);
            helpCallCommandListener.listenOn(getUserChat(client.getUserID()));
            helpCallCommandListener.listenOn(getUserChat(member.getUserID()));

            Messenger.sendMessage(HelpBotConstants.CONNECTED_TO_CALL, MessageSubmission.FormatEnum.MESSAGEML,
                    helpChat, symClient);

            Messenger.sendMessage(getRoomInfo(), MessageSubmission.FormatEnum.MESSAGEML,
                    helpChat, symClient);

            Messenger.sendMessage(getHelpSummary(), MessageSubmission.FormatEnum.MESSAGEML,
                    helpChat, symClient);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void endCall() {
        helpCallCommandListener.stopListening(getUserChat(client.getUserID()));
        helpCallCommandListener.stopListening(getUserChat(member.getUserID()));

        helpClientListener.listenOn(getUserChat(client.getUserID()));
        memberCommandListener.listenOn(getUserChat(member.getUserID()));

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL, MessageSubmission.FormatEnum.MESSAGEML,
                helpChat, symClient);

        client.setCall(null);
        client.setOnCall(false);

        member.setCall(null);
        member.setOnCall(false);
    }

    public CallTypes getCallType() {
        return CallTypes.HELP_CALL;
    }

    public String getRoomInfo(){
        String roomInfo = MLTypes.START_ML + "Room Info:" + MLTypes.BREAK + "   Client: ";

        if(client.getEmail() == "" || client.getEmail() == null) {
            roomInfo += client.getUserID().toString() + MLTypes.BREAK;
        }else{
            roomInfo += client.getEmail() + MLTypes.BREAK;
        }

        roomInfo += HelpBotConstants.MEMBER_LABEL + ": " + member.getEmail() + MLTypes.BREAK;

        return roomInfo + MLTypes.END_ML;
    }

    public String getHelpSummary(){
        return MLTypes.START_ML + "Help Summary:" + MLTypes.BREAK + client.getHelpSummary() + MLTypes.END_ML;
    }

    private Chat getUserChat(Long userID){
        Chat chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());

        Set<User> users = new HashSet<User>();
        try {

            users.add(symClient.getUsersClient().getUserFromId(userID));
            chat.setRemoteUsers(users);

            chat.setStream(symClient.getStreamsClient().getStream(users));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chat;
    }
}
