package org.symphonyoss.webroomdesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.webroomdesk.listeners.chat.WebCallChatListener;
import org.symphonyoss.webroomdesk.models.HelpBotSession;
import org.symphonyoss.webroomdesk.models.users.Member;
import org.symphonyoss.webroomdesk.models.users.WebClient;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.web.WebMessage;

/**
 * Created by nicktarsillo on 7/8/16.
 */
public class MultiChatWebCall extends MultiChatHelpCall {
    private final Logger logger = LoggerFactory.getLogger(MultiChatHelpCall.class);
    private WebClient client;
    private WebSessionListener webSessionListener;
    private ChatListener chatListener;
    private WebSessionListener helpSession;

    public MultiChatWebCall(Member member, WebClient client, HelpBotSession session) {
        super(member, client, session);

        this.client = client;
        this.helpSession = session.getHelpSession();
    }

    @Override
    public void initiateCall() {
        super.initiateCall();

        client.getWebSession().removeListener(helpSession);
        chatListener = new WebCallChatListener(member, client, symClient);
        try {
            String id =symClient.getStreamsClient().getStreamFromEmail(member.getEmail()).getId();
            symClient.getChatService().getChatByStream(id).registerListener(chatListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        webSessionListener = (message) -> sendToSymphony(message);
        client.getWebSession().registerListener(webSessionListener);

        memberCommandListener.stopListening(helpChat);

    }

    @Override
    public void endCall() {
        client.getWebSession().removeListener(webSessionListener);
        helpChat.removeListener(chatListener);

        client.getWebSession().removeListener(helpSession);

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
