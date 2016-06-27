package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.Arrays;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class MySettingsAction implements AiAction {

    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        Member member = MemberCache.getMember(message);
        userIdList.add(member.getUserID());

        aiResponseSequence.addResponse(new AiResponse(MLTypes.START_ML.toString() +
        MLTypes.START_BOLD + member.getEmail() + ": " + MLTypes.END_BOLD
                + MLTypes.BREAK + HelpBotConstants.SEE_HELP_LABEL + member.isSeeHelpRequests()
                + MLTypes.BREAK + HelpBotConstants.HIDE_IDENTITY_LABEL + member.isHideIdentity()
                + MLTypes.BREAK + HelpBotConstants.TAGS_LABEL + Arrays.toString(member.getTags().toArray())
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, userIdList));

        return aiResponseSequence;
    }



}
