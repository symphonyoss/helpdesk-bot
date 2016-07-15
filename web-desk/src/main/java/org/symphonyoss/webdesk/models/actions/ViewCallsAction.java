package org.symphonyoss.webdesk.models.actions;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;
import org.symphonyoss.webdesk.constants.WebDeskConstants;
import org.symphonyoss.webdesk.utils.CallCache;

/**
 * Created by nicktarsillo on 7/14/16.
 */
public class ViewCallsAction implements AiAction{

    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        userIdList.add(message.getFromUserId());

        aiResponseSequence.addResponse(new AiResponse(MLTypes.START_ML.toString() + WebDeskConstants.CALL_CACHE_LABEL
                + CallCache.listCache() + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML,
                userIdList));

        return aiResponseSequence;
    }

}
