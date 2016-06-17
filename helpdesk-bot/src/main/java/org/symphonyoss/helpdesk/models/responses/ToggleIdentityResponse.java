package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCash;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class ToggleIdentityResponse extends BotResponse {
    public ToggleIdentityResponse(String command, int numArguments) {
        super(command, numArguments);
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        Member member = MemberCash.getMember(message);
        member.setHideIdentity(!member.isHideIdentity());
        if (member.isHideIdentity())
            Messenger.sendMessage("Your identity will now be hidden.",
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        else
            Messenger.sendMessage("Your identity will now be shown.",
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());

        MemberCash.writeMember(member);
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberCash.hasMember(userID)
                && !MemberCash.getMember(userID).isOnCall();
    }
}
