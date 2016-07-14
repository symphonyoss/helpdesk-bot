package org.symphonyoss.proxydesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.proxydesk.constants.HelpBotConstants;
import org.symphonyoss.proxydesk.utils.CallCache;
import org.symphonyoss.proxydesk.utils.HoldCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 7/14/16.
 */
public class ViewCallsAction implements AiAction{

    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        userIdList.add(message.getFromUserId());

        aiResponseSequence.addResponse(new AiResponse(MLTypes.START_ML.toString() + HelpBotConstants.CALL_CACHE_LABEL
                + CallCache.listQueue() + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML,
                userIdList));

        return aiResponseSequence;
    }

}
