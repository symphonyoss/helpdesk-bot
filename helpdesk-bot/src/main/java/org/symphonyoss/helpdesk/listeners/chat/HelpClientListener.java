package org.symphonyoss.helpdesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.command.HelpClientCommandListener;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 * The main listener for dealing with clients requesting help
 */
public class HelpClientListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(AiCommandListener.class);
    private AiCommandListener helpResponseListener;
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient) {
        this.symClient = symClient;
        helpResponseListener = new HelpClientCommandListener(symClient);
    }

    /**
     * A method called from the chat listener, when a new chat message is received.
     * If the received message is not a command, relay the message to the onlines, off call
     * members.
     * @param message   the received message
     */
    public void onChatMessage(Message message) {
        logger.debug("Client {} sent help request message.", message.getFromUserId());
        if (helpResponseListener.isCommand(message))
            return;

        if (!HoldCache.hasClient(ClientCache.retrieveClient(message)))
            HoldCache.putClientOnHold(ClientCache.retrieveClient(message));

        MlMessageParser mlMessageParser;
        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());
        } catch (Exception e) {
            return;
        }

        String[] chunks = mlMessageParser.getTextChunks();

        ClientCache.retrieveClient(message).getHelpRequests().add(mlMessageParser.getText());

        for (Member member : MemberCache.MEMBERS.values())
            if (!member.isOnCall() && member.isSeeHelpRequests()) {
                if (ClientCache.retrieveClient(message).getEmail() != null &&
                        ClientCache.retrieveClient(message).getEmail().equals("")) {
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + ClientCache.retrieveClient(message).getEmail() +
                                    ": " + MLTypes.END_BOLD + String.join(" ", chunks) + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);
                } else {
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD + message.getFromUserId() + ": " + MLTypes.END_BOLD + String.join(" ", chunks) + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);
                }
            }
    }

    /**
     * Register this listener to the chat appropriately
     * @param chat the chat to register this listener on
     */
    public void listenOn(Chat chat) {
        helpResponseListener.listenOn(chat);
        chat.registerListener(this);
    }

    /**
     * Remove this listener from the chat appropriately
     * @param chat the chat to remove this listener from
     */
    public void stopListening(Chat chat) {
        helpResponseListener.stopListening(chat);
        chat.removeListener(this);
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
