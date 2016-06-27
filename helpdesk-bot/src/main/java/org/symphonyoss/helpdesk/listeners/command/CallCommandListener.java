package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.config.HelpBotConfig;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.actions.ExitAction;
import org.symphonyoss.helpdesk.models.actions.HelpSummaryAction;
import org.symphonyoss.helpdesk.models.actions.RoomInfoAction;
import org.symphonyoss.helpdesk.models.permissions.IsMember;
import static org.symphonyoss.helpdesk.config.HelpBotConfig.Config;

/**
 * Created by nicktarsillo on 6/20/16.
 * A extension of the ai command listener.
 * Initializes the required commands, used inside a call.
 */
public class CallCommandListener extends AiCommandListener {
    private Call call;

    public CallCommandListener(SymphonyClient symClient, Call call) {
        super(symClient);
        this.call = call;
        init();
    }

    private void init() {
        AiCommand exit = new AiCommand(Config.getString(HelpBotConfig.EXIT), 0);
        exit.addAction(new ExitAction(call));

        AiCommand sendInfo = new AiCommand(Config.getString(HelpBotConfig.ROOM_INFO), 0);
        sendInfo.addAction(new RoomInfoAction());

        AiCommand sendSummary = new AiCommand(Config.getString(HelpBotConfig.HELP_SUMMARY), 0);
        sendSummary.addAction(new HelpSummaryAction());
        sendSummary.addPermission(new IsMember());

        getActiveCommands().add(sendInfo);
        getActiveCommands().add(sendSummary);
        getActiveCommands().add(exit);
    }



}
