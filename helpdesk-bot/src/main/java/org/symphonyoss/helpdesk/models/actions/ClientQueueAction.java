package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/17/16.
 * An AiAction that allows a member to view the help clients currently in queue.
 */
public class ClientQueueAction implements AiAction {

    /**
     * Send back a message, representing the client queue.
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
        aiResponseSequence.addResponse(new AiResponse(MLTypes.START_ML.toString() + HelpBotConstants.CLIENT_QUEUE_LABEL
                + HoldCache.listQueue() + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML,
                userIdList));

        return aiResponseSequence;
    }



}
