package org.symphonyoss.webdesk.listeners.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webdesk.config.WebBotConfig;
import org.symphonyoss.webdesk.models.users.HelpClient;
import org.symphonyoss.webdesk.utils.ClientCache;
import org.symphonyoss.webdesk.utils.HoldCache;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.web.WebMessage;

/**
 * A web session listener for help-desk interaction
 */
public class WebHelpSessionListener implements WebSessionListener {
    private final Logger logger = LoggerFactory.getLogger(WebHelpSessionListener.class);
    private SymphonyClient symClient;

    public WebHelpSessionListener(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    /**
     * Listens on a web session.
     * Relays messages back to symphony member chat.
     *
     * @param webMessage the message relieved
     */
    public void onNewWSMessage(WebMessage webMessage) {
        try {

            logger.info("Sent message: " + webMessage.getMessage());
            Chat chat = symClient.getChatService().getChatByStream(System.getProperty(WebBotConfig.MEMBER_CHAT_STREAM));
            Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                            + webMessage.getEmail() + ": " + MLTypes.END_BOLD
                            + webMessage.getMessage() + MLTypes.END_ML,
                    MessageSubmission.FormatEnum.MESSAGEML,
                    chat, symClient);
            ;

            User user = symClient.getUsersClient().getUserFromEmail(webMessage.getEmail());
            HelpClient client = ClientCache.retrieveClient(user);
            client.getHelpRequests().add(webMessage.getMessage());
            if (!HoldCache.hasClient(client))
                HoldCache.putClientOnHold(client);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
