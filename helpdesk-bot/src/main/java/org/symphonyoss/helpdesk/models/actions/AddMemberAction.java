package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class AddMemberAction implements AiAction {
    private HelpClientListener helpClientListener;
    private AiCommandListener commandListener;
    private SymphonyClient symClient;

    public AddMemberAction(HelpClientListener helpClientListener, AiCommandListener aiCommandListener, SymphonyClient symClient) {
        this.helpClientListener = helpClientListener;
    }

    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList responseList = new AiResponseList();
        UserIdList userIdList = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();
        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

        try {
            User user = symClient.getUsersClient().getUserFromEmail(email);
            if (user != null && !MemberCache.hasMember(user.getId().toString())) {
                Member member = new Member(email,
                        user.getId());
                MemberCache.addMember(member);

                if (ClientCache.hasClient(user.getId()))
                    ClientCache.removeClient(user);

                userIdList.add(message.getFromUserId());
                responseList.addResponse(new AiResponse(HelpBotConstants.PROMOTED_USER + email
                        + HelpBotConstants.TO_MEMBER, MessageSubmission.FormatEnum.TEXT, userIdList));

                userIdList = new UserIdList();
                userIdList.add(user.getId());
                responseList.addResponse(new AiResponse(HelpBotConstants.PROMOTED,
                        MessageSubmission.FormatEnum.TEXT, userIdList));

                Chat chat = symClient.getChatService().getChatByStream(
                        symClient.getStreamsClient().getStream(userIdList).getId());
                helpClientListener.stopListening(chat);
                commandListener.listenOn(chat);
            } else {
                userIdList.add(message.getFromUserId());
                responseList.addResponse(new AiResponse(HelpBotConstants.PROMOTION_FAILED, MessageSubmission.FormatEnum.TEXT,
                        userIdList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseList;
    }

    public AiCommandListener getCommandListener() {
        return commandListener;
    }

    public void setCommandListener(AiCommandListener commandListener) {
        this.commandListener = commandListener;
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
