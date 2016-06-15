package org.symphonyoss.helpdesk.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.HelpDesk;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClientListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(BotResponseListener.class);
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public void onChatMessage(Message message) {
        logger.debug("Client {} sent help request message.", message.getFromUserId());

        MlMessageParser mlMessageParser;
        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());
        } catch (Exception e) {
            return;
        }

        String[] chunks = mlMessageParser.getTextChunks();

        HelpDesk.retrieveClient(message).getHelpRequests().add(message.getMessage());

        for (Member member : MemberDatabase.MEMBERS.values())
            if (!member.isOnCall() && member.isSeeCommands()) {
                if (HelpDesk.retrieveClient(message).getEmail() != null &&
                        HelpDesk.retrieveClient(message).getEmail() != "") {
                    Messenger.sendMessage(HelpBotConstants.START_ML + HelpBotConstants.START_BOLD
                                    + HelpDesk.retrieveClient(message).getEmail() +
                                    ": " + HelpBotConstants.END_BOLD + String.join(" ", chunks) + HelpBotConstants.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, member.getEmail(), symClient);
                } else {
                    Messenger.sendMessage(HelpBotConstants.START_ML + HelpBotConstants.START_BOLD + message.getFromUserId() + ": " + HelpBotConstants.END_BOLD + String.join(" ", chunks) + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, member.getEmail(), symClient);
                }
            }
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
