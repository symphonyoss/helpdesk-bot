package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class ExitAction implements AiAction {
    private Call call;

    public ExitAction(Call call) {
        this.call = call;
    }

    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList aiResponseList = new AiResponseList();

        Member member = MemberCache.getMember(message);
        HelpClient client = ClientCache.retrieveClient(message);

        if (member != null)
            call.exit(member);
        else
            call.exit(client);

        return aiResponseList;
    }
}
