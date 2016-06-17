package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.enums.DeskUserType;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.DeskUserCash;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/17/16.
 */
public class HelpSummaryResponse extends BotResponse {
    public HelpSummaryResponse(String command, int numArguments) {
        super(command, numArguments);
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        DeskUser deskUser = DeskUserCash.getDeskUser(message.getFromUserId().toString());
        deskUser.getCall().sendHelpSummary(message);
    }

    @Override
    public boolean userHasPermission(String userID) {
        return DeskUserCash.getDeskUser(userID).getUserType() == DeskUserType.MEMBER;
    }
}
