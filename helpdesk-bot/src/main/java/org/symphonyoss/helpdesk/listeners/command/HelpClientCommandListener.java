package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.config.HelpBotConfig;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.actions.OnlineMembersAction;

import static org.symphonyoss.helpdesk.config.HelpBotConfig.Config;

/**
 * Created by nicktarsillo on 6/20/16.
 * A extension of the ai command listener.
 * Initializes the required commands, used inside a help client listener.
 */
public class HelpClientCommandListener extends AiCommandListener {
    public HelpClientCommandListener(SymphonyClient symClient) {
        super(symClient);
        init();
    }

    private void init() {
        AiCommand command = new AiCommand(Config.getString(HelpBotConfig.ONLINE_MEMBERS), 0);
        command.addAction(new OnlineMembersAction());

        getActiveCommands().add(command);
    }



}
