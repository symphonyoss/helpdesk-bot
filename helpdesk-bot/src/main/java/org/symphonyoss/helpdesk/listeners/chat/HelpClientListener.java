package org.symphonyoss.helpdesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.responses.JoinChatResponse;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCash;
import org.symphonyoss.helpdesk.utils.MemberCash;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClientListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(BotResponseListener.class);
    private BotResponseListener botResponseListener;
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient) {
        this.symClient = symClient;
        botResponseListener = new BotResponseListener(symClient);

        JoinChatResponse joinChat = new JoinChatResponse("Join chat ", 1);
        joinChat.setPlaceHolder(0, "Client/Member");
        joinChat.setPrefixRequirement(0, "@");
        botResponseListener.getActiveResponses().add(joinChat);
    }

    public void onChatMessage(Message message) {
        logger.debug("Client {} sent help request message.", message.getFromUserId());
        if(botResponseListener.isCommand(message))
            return;

        MlMessageParser mlMessageParser;
        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());
        } catch (Exception e) {
            return;
        }

        String[] chunks = mlMessageParser.getTextChunks();

        ClientCash.retrieveClient(message).getHelpRequests().add(mlMessageParser.getText());

        for (Member member : MemberCash.MEMBERS.values())
            if (!member.isOnCall() && member.isSeeCommands()) {
                if (ClientCash.retrieveClient(message).getEmail() != null &&
                        ClientCash.retrieveClient(message).getEmail() != "") {
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + ClientCash.retrieveClient(message).getEmail() +
                                    ": " + MLTypes.END_BOLD + String.join(" ", chunks) + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);
                } else {
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD + message.getFromUserId() + ": " + MLTypes.END_BOLD + String.join(" ", chunks) + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);
                }
            }
    }

    public void listenOn(Chat chat) {
        chat.registerListener(botResponseListener);
        chat.registerListener(this);
    }

    public void stopListening(Chat chat) {
        chat.removeListener(botResponseListener);
        chat.removeListener(this);
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
