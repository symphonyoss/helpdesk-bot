package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.enums.DeskUserType;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class JoinChatResponse extends BotResponse {
    public JoinChatResponse(String command, int numArguments) {
        super(command, numArguments);
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        String[] chunks = mlMessageParser.getTextChunks();

        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(getPrefixRequirement(0)) + 1);

        DeskUser requester = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        DeskUser join = null;
        try {
            User user = listener.getSymClient().getUsersClient().getUserFromEmail(email);
            if (user != null)
                join = DeskUserCache.getDeskUser(user.getId().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (join != null && join.isOnCall()) {
            if (requester.getUserType() == DeskUserType.HELP_CLIENT) {
                join.getCall().enter(ClientCache.retrieveClient(message));
            } else if (requester.getUserType() == DeskUserType.MEMBER) {
                join.getCall().enter(MemberCache.getMember(message));
            }
        } else if (join != null && !join.isOnCall()) {
            Messenger.sendMessage(HelpBotConstants.NOT_ON_CALL, MessageSubmission.FormatEnum.TEXT,
                    message, listener.getSymClient());
        } else
            Messenger.sendMessage(email + HelpBotConstants.NOT_FOUND, MessageSubmission.FormatEnum.TEXT,
                    message, listener.getSymClient());
    }

    @Override
    public boolean userHasPermission(String userID) {
        return !DeskUserCache.getDeskUser(userID).isOnCall()
                && DeskUserCache.getDeskUser(userID).getUserType() == DeskUserType.MEMBER;
    }
}
