package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/16/16.
 * An AiAction taht allows a client or member to exit a call.
 */
public class ExitAction implements AiAction {
    private Call call;

    public ExitAction(Call call) {
        this.call = call;
    }

    /**
     * Find user by from message id.
     * Exit the call.
     *
     * @param mlMessageParser   the parser contains the input in ML
     * @param message   the received message
     * @param command   the command that triggered this action
     * @return   the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();

        Member member = MemberCache.getMember(message);
        HelpClient client = ClientCache.retrieveClient(message);

        if (member != null)
            call.exit(member);
        else
            call.exit(client);

        return aiResponseSequence;
    }
}
