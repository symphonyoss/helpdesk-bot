package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/17/16.
 * An AiAction that allows a member or client to see all online members.
 */
public class OnlineMembersAction implements AiAction {

    /**
     * Send back a message with all the online members.
     * Retain members identity preference.
     *
     * @param mlMessageParser   the parser contains the input in ML
     * @param message   the received message
     * @param command   the command that triggered this action
     * @return   the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();

        userIdList.add(message.getFromUserId());
        aiResponseSequence.addResponse(new AiResponse(MLTypes.START_ML.toString()
                + HelpBotConstants.MEMBERS_ONLINE + MemberCache.listOnlineMembers()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, userIdList));

        return aiResponseSequence;
    }

}
