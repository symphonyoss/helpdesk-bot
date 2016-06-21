package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/17/16.
 */
public class ClientQueueAction implements AiAction {
    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList aiResponseList = new AiResponseList();
        UserIdList userIdList = new UserIdList();

        userIdList.add(message.getFromUserId());
        aiResponseList.addResponse(new AiResponse(MLTypes.START_ML.toString() + HelpBotConstants.CLIENT_QUEUE_LABEL
                + HoldCache.listQueue() + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML,
                userIdList));

        return aiResponseList;
    }
}
