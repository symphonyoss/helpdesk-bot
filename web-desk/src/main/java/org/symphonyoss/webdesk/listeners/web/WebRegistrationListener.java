package org.symphonyoss.webdesk.listeners.web;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webdesk.config.WebBotConfig;
import org.symphonyoss.webdesk.constants.WebDeskConstants;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webdesk.utils.ClientCache;
import org.symphonyoss.webdesk.utils.HoldCache;
import org.symphonyoss.webdesk.utils.MemberCache;
import org.symphonyoss.webdesk.utils.WebClientCache;
import org.symphonyoss.webservice.listeners.SessionListener;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.session.Session;
import org.symphonyoss.webservice.models.session.WebSession;
import org.symphonyoss.webservice.models.web.WebMessage;

import java.util.HashSet;

/**
 * Registers an opened web session.
 * Place in cache.
 * Listen on help session.
 */
public class WebRegistrationListener implements SessionListener {
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;
    private WebSessionListener webSessionListener;

    public WebRegistrationListener(SymphonyClient symClient,
                                   WebSessionListener webSessionListener,
                                   HelpClientListener helpClientListener) {
        this.symClient = symClient;
        this.helpClientListener = helpClientListener;
        this.webSessionListener = webSessionListener;
    }

    @Override
    public void onSessionInit(Session session) {

        try {

            User user = symClient.getUsersClient().getUserFromEmail(session.getSessionData().getEmail());

            if (user != null
                    && session.getSessionType() == Session.SessionType.WEB
                    && !MemberCache.hasMember(user.getId().toString())) {

                HoldCache.putClientOnHold(WebClientCache.addClient(user, (WebSession) session));

                Chat chat = new Chat();
                chat.setRemoteUsers(new HashSet<>());
                chat.setStream(symClient.getStreamsClient().getStream(user));
                symClient.getChatService().addChat(chat);
                helpClientListener.listenOn(chat);

                chat = symClient.getChatService().getChatByStream(System.getProperty(WebBotConfig.MEMBER_CHAT_STREAM));
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + session.getSessionData().getEmail() + MLTypes.END_BOLD
                                + WebDeskConstants.OPENED_SESSION + WebDeskConstants.TOPIC
                                + session.getSessionData().getTopic() + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, chat, symClient);

                ((WebSession) session).registerListener(webSessionListener);

            } else {

                if (session.getSessionType() == Session.SessionType.WEB) {
                    ((WebSession) session).sendMessageToWebService(new WebMessage(
                            System.currentTimeMillis(), "HelpBot", "",
                            WebDeskConstants.COULD_NOT_LOCATE));
                }

                session.terminateSession();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSessionTerminate(Session session) {
        try {
            User user = symClient.getUsersClient().getUserFromEmail(session.getSessionData().getEmail());

            if (ClientCache.hasClient(user.getId())
                    && HoldCache.hasClient(ClientCache.retrieveClient(user)))
                HoldCache.pickUpClient(WebClientCache.removeClient(user));
            else {
                WebClientCache.removeClient(user);
            }

            if (session.getSessionType() == Session.SessionType.WEB) {

                ((WebSession) session).removeListener(webSessionListener);

                Chat chat = new Chat();
                chat.setRemoteUsers(new HashSet<>());
                chat.setStream(symClient.getStreamsClient().getStream(user));
                symClient.getChatService().addChat(chat);
                helpClientListener.stopListening(chat);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}