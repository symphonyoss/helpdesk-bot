package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.calls.HelpCall;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/17/16.
 * An AiAction that provides a member or client with information about the room.
 */
public class RoomInfoAction implements AiAction {

    /**
     * Send back a message containing all the information about the room.
     * Includes all clients in room.
     * Includes all members in room.
     * Retain member identity preference.
     *
     * @param mlMessageParser   the parser contains the input in ML
     * @param message   the received message
     * @param command   the command that triggered this action
     * @return   the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();

        DeskUser deskUser = DeskUserCache.getDeskUser(message.getFromUserId().toString());
        ((HelpCall) deskUser.getCall()).getHelpCallResponder().sendRoomInfo(message.getFromUserId());

        return aiResponseSequence;
    }
}
