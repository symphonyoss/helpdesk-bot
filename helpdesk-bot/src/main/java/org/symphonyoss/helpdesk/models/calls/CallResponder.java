package org.symphonyoss.helpdesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.command.CallCommandListener;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/21/16.
 * A model that allows a call to relay a message to both parties (Member and Client).
 */
public class CallResponder {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    private CallCommandListener callCommandListener;
    private Call call;
    protected SymphonyClient symClient;

    public CallResponder(Call call, SymphonyClient symClient) {
        this.symClient = symClient;
        this.call = call;
    }

    /**
     * Notify a user that they have successfully connected to the room
     *
     * @param user   the desk user to send to
     */
    public void sendConnectedMessage(DeskUser user) {
        if(user == null){

            if(logger != null)
                logger.error("Cannot send null user connected message {}.", user);

            return;
        }

        Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.CONNECTED_TO_CALL
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

    }

    public void sendLeftCallMessage(Long userID) {
        if(userID == null){

            if(logger != null)
                logger.error("Cannot send null user left message {}.", userID);

            return;
        }

        Messenger.sendMessage(HelpBotConstants.EXIT_CALL,
                MessageSubmission.FormatEnum.TEXT, userID, symClient);
    }
}
