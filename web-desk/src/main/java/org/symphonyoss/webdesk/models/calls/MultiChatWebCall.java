package org.symphonyoss.webdesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.webdesk.listeners.chat.WebCallChatListener;
import org.symphonyoss.webdesk.models.HelpBotSession;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.models.users.WebClient;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.web.WebMessage;

/**
 * A chat model for web calls.
 */
public class MultiChatWebCall extends MultiChatHelpCall {
    private final Logger logger = LoggerFactory.getLogger(MultiChatHelpCall.class);
    private WebClient webClient;
    private WebSessionListener webRelayListener;
    private ChatListener memberRelayListener;
    private WebSessionListener webHelpSessionListener;

    public MultiChatWebCall(Member member, WebClient client, HelpBotSession session) {
        super(member, client, session);

        this.webClient = client;
        this.webHelpSessionListener = session.getHelpSession();
    }

    /**
     * Provides additional action to the initiate call method
     */
    @Override
    public void initiateCall() {
        super.initiateCall();

        if(webClient == null
                || member == null){

            if(logger != null)
                logger.warn("Call started when member or client were null.");

            return;
        }

        webClient.getWebSession().removeListener(webHelpSessionListener);
        memberRelayListener = new WebCallChatListener(member, webClient, symClient);
        try {
            String id = symClient.getStreamsClient().getStreamFromEmail(member.getEmail()).getId();
            symClient.getChatService().getChatByStream(id).registerListener(memberRelayListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        webRelayListener = (message) -> sendToSymphony(message);
        webClient.getWebSession().registerListener(webRelayListener);

        memberCommandListener.stopListening(helpChat);

    }

    @Override
    public void endCall() {

        if(webClient == null
                || member == null){

            if(logger != null)
                logger.warn("Call end when member or client were null.");

            return;
        }

        webClient.getWebSession().removeListener(webRelayListener);

        getUserChat(member.getUserID()).removeListener(memberRelayListener);
        getUserChat(webClient.getUserID()).removeListener(memberRelayListener);

        webClient.getWebSession().registerListener(webHelpSessionListener);

        logger.info("Ended Chat.");

        super.endCall();
    }

    private void sendToSymphony(WebMessage webMessage) {

        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                        + webMessage.getFrom() + ": " + MLTypes.END_BOLD
                        + webMessage.getMessage() + MLTypes.END_ML,
                MessageSubmission.FormatEnum.MESSAGEML, helpChat, symClient);

    }

}
