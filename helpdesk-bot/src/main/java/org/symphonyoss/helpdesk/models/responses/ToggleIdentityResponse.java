package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;
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
        Member member = MemberCache.getMember(message);
        member.setHideIdentity(!member.isHideIdentity());
        if (member.isHideIdentity())
            Messenger.sendMessage(HelpBotConstants.HIDE_IDENTITY,
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        else
            Messenger.sendMessage(HelpBotConstants.SHOW_IDENTITY,
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());

        MemberCache.writeMember(member);
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberCache.hasMember(userID)
                && !MemberCache.getMember(userID).isOnCall();
    }
}
