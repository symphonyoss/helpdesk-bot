package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallCache;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
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

        Member member = MemberCache.getMember(message);
        if (chunks.length > getCommand().split(" ").length) {
            String email = String.join(" ", chunks);
            email = email.substring(email.indexOf(getPrefixRequirement(0)) + 1);

            HelpClient helpClient = HoldCache.findClientCredentialMatch(email);

            if (helpClient != null) {
                CallCache.newCall(member, HoldCache.pickUpClient(helpClient), listener, helpListener, listener.getSymClient());
            } else
                Messenger.sendMessage(email + HelpBotConstants.NOT_FOUND,
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        } else {
            if (HoldCache.ONHOLD.size() > 0) {
                CallCache.newCall(member, HoldCache.pickUpNextClient(), listener, helpListener, listener.getSymClient());
            } else
                Messenger.sendMessage(HelpBotConstants.NO_USERS,
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        }
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberCache.hasMember(userID)
                && !MemberCache.getMember(userID).isOnCall();
    }

    public HelpClientListener getHelpListener() {
        return helpListener;
    }

    public void setHelpListener(HelpClientListener helpListener) {
        this.helpListener = helpListener;
    }
}
