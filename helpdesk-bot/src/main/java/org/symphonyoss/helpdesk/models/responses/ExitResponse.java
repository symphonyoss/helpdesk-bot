package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientDatabase;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class ExitResponse extends BotResponse {
    private Call call;

    public ExitResponse(String command, int numArguments, Call call) {
        super(command, numArguments);
        this.call = call;
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        Member member = MemberDatabase.getMember(message);
        HelpClient client = ClientDatabase.retrieveClient(message);

        if(member  != null)
            call.exit(member);
        else
            call.exit(client);
    }

    @Override
    public boolean userHasPermission(String userID) {
        return true;
    }
}
