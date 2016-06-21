package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.CallCache;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class AcceptHelpAction implements AiAction {
    private HelpClientListener helpListener;
    private AiCommandListener commandListener;
    private SymphonyClient symClient;

    public AcceptHelpAction(HelpClientListener helpListener, AiCommandListener commandListener, SymphonyClient symClient) {
        this.helpListener = helpListener;
        this.commandListener = commandListener;
        this.symClient = symClient;
    }

    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList aiResponseList = new AiResponseList();
        UserIdList sendTo = new UserIdList();

        String[] chunks = mlMessageParser.getTextChunks();

        Member member = MemberCache.getMember(message);
        if (chunks.length > command.getCommand().split(" ").length) {
            String email = String.join(" ", chunks);
            email = email.substring(email.indexOf(command.getPrefixRequirement(0)) + 1);

            HelpClient helpClient = HoldCache.findClientCredentialMatch(email);

            if (helpClient != null) {
                CallCache.newCall(member, HoldCache.pickUpClient(helpClient), commandListener, helpListener, symClient);
            } else {
                sendTo.add(message.getFromUserId());
                aiResponseList.addResponse(new AiResponse(email + HelpBotConstants.NOT_FOUND,
                        MessageSubmission.FormatEnum.TEXT, sendTo));
            }
        } else {
            if (HoldCache.ONHOLD.size() > 0) {
                CallCache.newCall(member, HoldCache.pickUpNextClient(), commandListener, helpListener, symClient);
            } else {
                sendTo.add(message.getFromUserId());
                aiResponseList.addResponse(new AiResponse(HelpBotConstants.NO_USERS,
                        MessageSubmission.FormatEnum.TEXT, sendTo));
            }
        }
        return aiResponseList;
    }

    public HelpClientListener getHelpListener() {
        return helpListener;
    }

    public void setHelpListener(HelpClientListener helpListener) {
        this.helpListener = helpListener;
    }

    public AiCommandListener getAiCommandListener() {
        return commandListener;
    }

    public void setAiCommandListener(AiCommandListener aiCommandListener) {
        this.commandListener = aiCommandListener;
    }
}