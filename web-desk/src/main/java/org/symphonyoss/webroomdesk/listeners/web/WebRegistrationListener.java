package org.symphonyoss.webroomdesk.listeners.web;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webroomdesk.config.HelpBotConfig;
import org.symphonyoss.webroomdesk.constants.HelpBotConstants;
import org.symphonyoss.webroomdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webroomdesk.models.HelpBotSession;
import org.symphonyoss.webroomdesk.models.users.WebClient;
import org.symphonyoss.webroomdesk.utils.WebClientCache;
import org.symphonyoss.webservice.listeners.SessionListener;
import org.symphonyoss.webservice.listeners.WebSessionListener;
import org.symphonyoss.webservice.models.session.Session;
import org.symphonyoss.webservice.models.session.WebSession;
import org.symphonyoss.webservice.models.web.WebMessage;

import java.util.HashSet;

/**
 * Created by nicktarsillo on 7/8/16.
 */
public class WebRegistrationListener implements SessionListener {
    private HelpClientListener helpClientListener;
    private SymphonyClient symClient;
    private WebSessionListener helpSession;

    public WebRegistrationListener(SymphonyClient symClient,
                                   WebSessionListener helpSession,
                                   HelpClientListener helpClientListener){
        this.symClient = symClient;
        this.helpClientListener = helpClientListener;
        this.helpSession = helpSession;
    }

    @Override
    public void onSessionInit(Session session) {

        try {

            User user = symClient.getUsersClient().getUserFromEmail(session.getSessionData().getEmail());

            if(user != null
                    && session.getSessionType() == Session.SessionType.WEB ){

                WebClientCache.addClient(user, (WebSession) session);

                Chat chat = new Chat();
                chat.setRemoteUsers(new HashSet<>());
                chat.setStream(symClient.getStreamsClient().getStream(user));
                symClient.getChatService().addChat(chat);
                helpClientListener.listenOn(chat);

                ((WebSession) session).registerListener(helpSession);

            }else{

                if(session.getSessionType() == Session.SessionType.WEB) {
                    ((WebSession) session).sendMessageToWebService(new WebMessage(
                            System.currentTimeMillis(), "HelpBot", "",
                            HelpBotConstants.COULD_NOT_LOCATE));
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
            WebClientCache.removeClient(user);

            if(session.getSessionType() == Session.SessionType.WEB)
                ((WebSession) session).removeListener(helpSession);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}