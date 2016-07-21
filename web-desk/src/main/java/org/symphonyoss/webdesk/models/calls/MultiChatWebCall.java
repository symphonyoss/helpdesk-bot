package org.symphonyoss.webdesk.models.calls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.webdesk.constants.WebDeskConstants;
import org.symphonyoss.webdesk.listeners.chat.WebCallChatListener;
import org.symphonyoss.webdesk.models.HelpBotSession;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.models.users.WebClient;
import org.symphonyoss.webdesk.utils.CallCache;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.web.WebMessage;

/**
 * A chat model for web calls.
 */
public class MultiChatWebCall extends MultiChatHelpCall {
    private final Logger logger = LoggerFactory.getLogger(MultiChatHelpCall.class);
    private WebClient webClient;
    private WebSessionListener webRelayListener;
    private WebCallChatListener memberRelayListener;
    private WebSessionListener webHelpSessionListener;

    public MultiChatWebCall(Member member, WebClient client, HelpBotSession session) {
        super(member, client, session);

        this.webClient = client;
        this.webHelpSessionListener = session.getHelpSession();
    }

    /**
     * Provides additional actions to the initiate call method
     * Remove Web Help Session listener
     * Add new WS to symphony chat listener
     * Add new symphony to WS chat listener
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

        getUserChat(member.getUserID()).registerListener(memberRelayListener);

        helpChat.registerListener(memberRelayListener);

        webClient.getWebSession().sendMessageToWebService(new WebMessage(System.currentTimeMillis(),
                "HelpBot", "", WebDeskConstants.CONNECTED_TO_CALL));

        webClient.getWebSession().sendMessageToWebService(new WebMessage(System.currentTimeMillis(),
                "HelpBot", "", super.getRoomInfo()));

        webRelayListener = (message) -> sendToSymphony(message);
        webClient.getWebSession().registerListener(webRelayListener);

        memberCommandListener.stopListening(getUserChat(member.getUserID()));
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

        getUserChat(member.getUserID()).removeListener(memberRelayListener);

        helpChat.removeListener(memberRelayListener);

        webClient.getWebSession().removeListener(webRelayListener);

        webClient.getWebSession().registerListener(webHelpSessionListener);

        symClient.getChatService().removeChat(helpChat);

        webClient.getWebSession().sendMessageToWebService(new WebMessage(System.currentTimeMillis(),
                "HelpBot", "", "Member has left the call. Help session ended."));

        logger.info("Ended Chat.");

        super.endCall();
    }

    private void sendToSymphony(WebMessage webMessage) {

        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                        + webMessage.getFrom() + ": " + MLTypes.END_BOLD
                        + webMessage.getMessage() + MLTypes.END_ML,
                MessageSubmission.FormatEnum.MESSAGEML, helpChat, symClient);

    }

    @Override
    public String toString(){
        String text = "Web Call " + (CallCache.getCallID(this)+1) + ": [ Member: ";
        if(!member.isUseAlias())
            text += member.getEmail() + ", ";
        else
            text += member.getAlias() + ", ";

        text = text.substring(0, text.length() -2) + " | Web Client: ";
        text += webClient.getEmail() + ", ";

        return text.substring(0, text.length() - 2) + " ]";
    }
}
