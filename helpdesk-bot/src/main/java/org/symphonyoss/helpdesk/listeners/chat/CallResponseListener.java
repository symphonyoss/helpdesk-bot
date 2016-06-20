package org.symphonyoss.helpdesk.listeners.chat;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.models.responses.ExitResponse;
import org.symphonyoss.helpdesk.models.responses.HelpSummaryResponse;
import org.symphonyoss.helpdesk.models.responses.RoomInfoResponse;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class CallResponseListener extends BotResponseListener{
    private Call call;

    public CallResponseListener(SymphonyClient symClient, Call call){
        super(symClient);
        this.call = call;
        init();
    }

    private  void init(){
        ExitResponse exitResponse = new ExitResponse("Exit", 0, call);

        RoomInfoResponse sendInfo = new RoomInfoResponse("Room Info", 0);

        HelpSummaryResponse sendSummary = new HelpSummaryResponse("Help Summary", 0);

        getActiveResponses().add(sendInfo);
        getActiveResponses().add(sendSummary);
        getActiveResponses().add(exitResponse);

    }
}
