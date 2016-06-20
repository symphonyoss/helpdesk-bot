package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/17/16.
 */
public class OnlineMembersResponse extends BotResponse {

    public OnlineMembersResponse(String command, int numArguments) {
        super(command, numArguments);
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        Messenger.sendMessage(MLTypes.START_ML.toString() + HelpBotConstants.MEMBERS_ONLINE
                        + MemberCache.listOnlineMembers() + MLTypes.END_ML,
                MessageSubmission.FormatEnum.MESSAGEML, message, listener.getSymClient());
    }

    @Override
    public boolean userHasPermission(String userID) {
        return !DeskUserCache.getDeskUser(userID).isOnCall();
    }
}
