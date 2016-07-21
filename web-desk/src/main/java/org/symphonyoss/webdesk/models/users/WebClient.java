package org.symphonyoss.webdesk.models.users;

import org.symphonyoss.webservice.models.session.WebSession;

/**
 * Created by nicktarsillo on 7/8/16.
 * An anonymous client using the web service to get help from the help desk
 */
public class WebClient extends HelpClient {
    private WebSession webSession;

    public WebClient(String email, Long userID, WebSession webSession) {
        super(email, userID);
        this.webSession = webSession;
    }

    public WebSession getWebSession() {
        return webSession;
    }

    public void setWebSession(WebSession webSession) {
        this.webSession = webSession;
    }

    @Override
    public DeskUserType getUserType() {
        return DeskUserType.WEB_CLIENT;
    }
}
