package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class ToggleSeeHelpResponse extends BotResponse {
    public ToggleSeeHelpResponse(String command, int numArguments) {
        super(command, numArguments);
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        Member member = MemberDatabase.getMember(message);
        member.setSeeCommands(!member.isSeeCommands());
        if (member.isSeeCommands())
            Messenger.sendMessage("You will now see help request messages.",
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        else
            Messenger.sendMessage("You will no longer see help requests.",
                    MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());

        MemberDatabase.writeMember(member);
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberDatabase.hasMember(userID)
                && !MemberDatabase.getMember(userID).isOnCall();
    }
}
