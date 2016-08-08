package org.symphonyoss.webdesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;
import org.symphonyoss.webdesk.constants.WebDeskConstants;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webdesk.listeners.chat.TranscriptListener;
import org.symphonyoss.webdesk.listeners.command.HelpCallCommandListener;
import org.symphonyoss.webdesk.listeners.command.MemberCommandListener;
import org.symphonyoss.webdesk.models.HelpBotSession;
import org.symphonyoss.webdesk.models.users.HelpClient;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.utils.CallCache;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A model that represents a call between a member and a client.
 */
public class MultiChatHelpCall extends MultiChatCall {
    private final Logger logger = LoggerFactory.getLogger(MultiChatHelpCall.class);

    protected SymphonyClient symClient;
    protected Chat helpChat;
    protected MemberCommandListener memberCommandListener;
    protected Member member;
    private HelpCallCommandListener helpCallCommandListener;
    private HelpClientListener helpClientListener;
    private TranscriptListener transcriptListener;
    private HelpClient client;


    private Timer callTimer;

    public MultiChatHelpCall(Member member, HelpClient client, HelpBotSession session) {
        super();
        this.member = member;
        this.client = client;
        this.symClient = session.getSymphonyClient();

        this.memberCommandListener = session.getMemberListener();
        this.helpClientListener = session.getHelpClientListener();
        this.transcriptListener = session.getTranscriptListener();

        this.callTimer = new Timer();

    }

    /**
     * Starts the call.
     * Cross listeners.
     * Add new command listener.
     * Set on call.
     * Send initial chat messages from the bot.
     */
    public void initiateCall() {
        if(client == null
                || member == null){

            if(logger != null)
                logger.warn("Cal started when member or client were null.");

            return;
        }

        callTimer.start();

        helpChat = new Chat();
        helpChat.setLocalUser(symClient.getLocalUser());

        client.setCall(this);
        client.setOnCall(true);

        member.setCall(this);
        member.setOnCall(true);

        Set<User> users = new HashSet<User>();

        try {

            users.add(symClient.getUsersClient().getUserFromId(client.getUserID()));
            users.add(symClient.getUsersClient().getUserFromId(member.getUserID()));

            helpChat.setRemoteUsers(users);
            helpChat.setStream(symClient.getStreamsClient().getStream(users));

            symClient.getChatService().addChat(helpChat);

            helpClientListener.stopListening(getUserChat(client.getUserID()));
            memberCommandListener.stopListening(getUserChat(member.getUserID()));

            helpCallCommandListener = new HelpCallCommandListener(symClient, this);
            helpCallCommandListener.listenOn(getUserChat(client.getUserID()));
            helpCallCommandListener.listenOn(getUserChat(member.getUserID()));

            helpChat.registerListener(transcriptListener);

            Messenger.sendMessage(WebDeskConstants.CONNECTED_TO_CALL, MessageSubmission.FormatEnum.TEXT,
                    helpChat, symClient);

            Messenger.sendMessage(getRoomInfo(), MessageSubmission.FormatEnum.MESSAGEML,
                    helpChat, symClient);

            Messenger.sendMessage(getHelpSummary(), MessageSubmission.FormatEnum.MESSAGEML,
                    helpChat, symClient);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * End the call.
     * Cross listeners back.
     * Notify that the call has ended.
     */
    public void endCall() {
        if(client == null
                || member == null
                || callTimer == null){

            if(logger != null)
                logger.warn("Cal started when member or client were null.");

            return;
        }

        callTimer.stop();
        callTimer = null;

        helpCallCommandListener.stopListening(getUserChat(client.getUserID()));
        helpCallCommandListener.stopListening(getUserChat(member.getUserID()));

        helpClientListener.listenOn(getUserChat(client.getUserID()));

        memberCommandListener.listenOn(getUserChat(member.getUserID()));

        helpChat.removeListener(transcriptListener);

        Messenger.sendMessage(WebDeskConstants.EXIT_CALL, MessageSubmission.FormatEnum.TEXT,
                helpChat, symClient);

        client.setCall(null);
        client.setOnCall(false);
        client.setHelpRequests(new LinkedHashSet<>());

        member.setCall(null);
        member.setOnCall(false);
    }

    public Chat getCallChat() {
        return helpChat;
    }

    public CallTypes getCallType() {
        return CallTypes.HELP_CALL;
    }

    /**
     * Parses a string to contain the a description of the rooms current
     * state,
     *
     * @return the room info
     */
    public String getRoomInfo() {
        String roomInfo = MLTypes.START_ML + "Room Info:" + MLTypes.BREAK + "   Client: ";

        if (client.getEmail() == "" || client.getEmail() == null) {
            roomInfo += client.getUserID().toString() + MLTypes.BREAK;
        } else {
            roomInfo += client.getEmail() + MLTypes.BREAK;
        }

        if(!member.isUseAlias())
            roomInfo += "   " + WebDeskConstants.MEMBER_LABEL + ": " + member.getEmail() + MLTypes.BREAK;
        else
            roomInfo += "   " + WebDeskConstants.MEMBER_LABEL + ": " + member.getAlias() + MLTypes.BREAK;

        return roomInfo + MLTypes.END_ML;
    }

    /**
     * Gets the help summary from the client.
     *
     * @return the help summary
     */
    public String getHelpSummary() {
        return MLTypes.START_ML + "Help Summary:" + MLTypes.BREAK + client.getHelpSummary() + MLTypes.END_ML;
    }

    /**
     * Get the one on one user web.
     *
     * @param userID the user's ID
     * @return the web the user belongs to
     */
    protected Chat getUserChat(Long userID) {
        UserIdList list = new UserIdList();
        list.add(userID);
        Stream stream = null;
        try {
            stream = symClient.getStreamsClient().getStream(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return symClient.getChatService().getChatByStream(stream.getId());
    }

    @Override
    public String toString(){
        String text = "Call " + (CallCache.getCallID(this)+1) + ": [ Member: ";
        if(!member.isUseAlias())
            text += member.getEmail() + ", ";
        else
            text += member.getAlias() + ", ";

        text = text.substring(0, text.length() -2) + " | Client: ";
        text += client.getEmail() + ", ";

        return text.substring(0, text.length() - 2) + " ]";
    }

    public Timer getCallTimer() {
        return callTimer;
    }

    public void setCallTimer(Timer callTimer) {
        this.callTimer = callTimer;
    }
}
