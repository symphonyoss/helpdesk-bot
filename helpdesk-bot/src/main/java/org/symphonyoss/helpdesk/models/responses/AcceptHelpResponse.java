package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallCash;
import org.symphonyoss.helpdesk.utils.HoldCash;
import org.symphonyoss.helpdesk.utils.MemberCash;
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

        Member member = MemberCash.getMember(message);
        if (chunks.length > getCommand().split(" ").length) {
            String email = String.join(" ", chunks);
            email = email.substring(email.indexOf(getPrefixRequirement(0)) + 1);

            HelpClient helpClient = HoldCash.findClientCredentialMatch(email);

            if (helpClient != null) {
                CallCash.newCall(member, HoldCash.pickUpClient(helpClient), listener, helpListener);
            } else
                Messenger.sendMessage(email + " does not exist, or has not requested help.",
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        } else {
            if (HoldCash.ONHOLD.size() > 0) {
                CallCash.newCall(member, HoldCash.pickUpNextClient(), listener, helpListener);
            } else
                Messenger.sendMessage("There are no users that need help currently.",
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        }
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberCash.hasMember(userID)
                && !MemberCash.getMember(userID).isOnCall();
    }

    public HelpClientListener getHelpListener() {
        return helpListener;
    }

    public void setHelpListener(HelpClientListener helpListener) {
        this.helpListener = helpListener;
    }
}
