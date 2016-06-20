package org.symphonyoss.helpdesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.HoldCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClientListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(BotResponseListener.class);
    private BotResponseListener helpResponseListener;
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient) {
        this.symClient = symClient;
        helpResponseListener = new HelpClientResponseListener(symClient);
    }

    public void onChatMessage(Message message) {
        logger.debug("Client {} sent help request message.", message.getFromUserId());
        if (helpResponseListener.isCommand(message))
            return;

        if(!HoldCache.hasClient(ClientCache.retrieveClient(message)))
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
            if (!member.isOnCall() && member.isSeeCommands()) {
                if (ClientCache.retrieveClient(message).getEmail() != null &&
                        ClientCache.retrieveClient(message).getEmail() != "") {
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

    public void listenOn(Chat chat) {
        helpResponseListener.listenOn(chat);
        chat.registerListener(this);
    }

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
