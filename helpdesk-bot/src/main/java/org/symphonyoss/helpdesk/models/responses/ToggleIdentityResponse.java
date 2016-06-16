package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.chat.BotResponseListener;
import org.symphonyoss.helpdesk.models.BotResponse;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
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
        Member member = MemberDatabase.getMember(message);
        member.setHideIdentity(!member.isHideIdentity());
        if (member.isHideIdentity())
            Messenger.sendMessage("Your identity will now be hidden.",
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        else
            Messenger.sendMessage("Your identity will now be shown.",
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberDatabase.hasMember(userID)
                && !MemberDatabase.getMember(userID).isOnCall();
    }
}
