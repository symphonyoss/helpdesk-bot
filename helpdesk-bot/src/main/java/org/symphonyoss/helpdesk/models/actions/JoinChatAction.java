package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class JoinChatAction implements AiAction {
    private SymphonyClient symClient;

    public JoinChatAction(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList aiResponseList = new AiResponseList();
        UserIdList userIdList = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();

        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

        DeskUser requester = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        DeskUser join = null;
        try {
            User user = symClient.getUsersClient().getUserFromEmail(email);
            if (user != null)
                join = DeskUserCache.getDeskUser(user.getId().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (join != null && join.isOnCall()) {
            if (requester.getUserType() == DeskUser.DeskUserType.HELP_CLIENT) {
                join.getCall().enter(ClientCache.retrieveClient(message));
            } else if (requester.getUserType() == DeskUser.DeskUserType.MEMBER) {
                join.getCall().enter(MemberCache.getMember(message));
            }
        } else if (join != null && !join.isOnCall()) {
            userIdList.add(message.getFromUserId());
            aiResponseList.addResponse(new AiResponse(HelpBotConstants.NOT_ON_CALL, MessageSubmission.FormatEnum.TEXT,
                    userIdList));
        } else {
            userIdList.add(message.getFromUserId());
            aiResponseList.addResponse(new AiResponse(email + HelpBotConstants.NOT_FOUND, MessageSubmission.FormatEnum.TEXT,
                    userIdList));
        }

        return aiResponseList;
    }
}
