package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.chat.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.BotResponse;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallDesk;
import org.symphonyoss.helpdesk.utils.HoldDesk;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class AcceptHelpResponse extends BotResponse {
    private HelpClientListener helpListener;

    public AcceptHelpResponse(String command, int numArguments, HelpClientListener helpListener) {
        super(command, numArguments);
        this.helpListener = helpListener;
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        String[] chunks = mlMessageParser.getTextChunks();

        Member member = MemberDatabase.getMember(message);
        if (chunks.length > getCommand().split(" ").length) {
            String id = String.join(" ", chunks);
            id = id.substring(id.indexOf(getPrefixRequirement(0)));

            HelpClient helpClient = HoldDesk.findClientCredentialMatch(id);

            if (helpClient != null) {
                CallDesk.newCall(member, HoldDesk.pickUpClient(helpClient), listener, helpListener);
            } else
                Messenger.sendMessage(id + " does not exist, or has not requested help.",
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        } else {
            if (HoldDesk.ONHOLD.size() > 0) {
                CallDesk.newCall(member, HoldDesk.pickUpNextClient(), listener, helpListener);
            } else
                Messenger.sendMessage("There are no users that need help currently.",
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        }
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberDatabase.hasMember(userID)
                && !MemberDatabase.getMember(userID).isOnCall();
    }

    public HelpClientListener getHelpListener() {
        return helpListener;
    }

    public void setHelpListener(HelpClientListener helpListener) {
        this.helpListener = helpListener;
    }
}
