package org.symphonyoss.helpdesk.listeners.chat;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.models.responses.OnlineMembersResponse;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class HelpClientResponseListener extends BotResponseListener {
    public HelpClientResponseListener(SymphonyClient symClient){
        super(symClient);
        init();
    }

    private void init(){
        OnlineMembersResponse onlineMembersResponse = new OnlineMembersResponse("Online Members ", 0);

        getActiveResponses().add(onlineMembersResponse);
    }

}
