package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.models.actions.OnlineMembersAction;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class HelpClientResponseListener extends AiCommandListener {
    public HelpClientResponseListener(SymphonyClient symClient) {
        super(symClient);
        init();
    }

    private void init() {
        AiCommand command = new AiCommand("Online Members", 0);
        command.addAction(new OnlineMembersAction());

        getActiveCommands().add(command);
    }

}
