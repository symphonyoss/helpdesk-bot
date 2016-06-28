package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.calls.HelpCall;
import org.symphonyoss.helpdesk.models.calls.HelpCallResponder;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/17/16.
 * A AiAction that returns back a summary of requested help from all the clients in a call.
 */
public class HelpSummaryAction implements AiAction {

    /**
     * Send back the summary of help requested from all the clients in the room.
     *
     * @param mlMessageParser   the parser contains the input in ML
     * @param message   the received message
     * @param command   the command that triggered this action
     * @return   the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();

        DeskUser deskUser = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        if(deskUser != null) {
            ((HelpCall)deskUser.getCall()).getHelpCallResponder().sendHelpSummary(message.getFromUserId());
        }

        return aiResponseSequence;
    }



}
