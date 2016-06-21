package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/17/16.
 */
public class HelpSummaryAction implements AiAction {
    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList aiResponseList = new AiResponseList();

        DeskUser deskUser = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        deskUser.getCall().getCallResponder().sendHelpSummary(message);

        return aiResponseList;
    }

}
