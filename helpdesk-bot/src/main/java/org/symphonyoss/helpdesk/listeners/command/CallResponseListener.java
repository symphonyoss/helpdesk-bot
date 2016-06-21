package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.actions.ExitAction;
import org.symphonyoss.helpdesk.models.actions.HelpSummaryAction;
import org.symphonyoss.helpdesk.models.actions.RoomInfoAction;
import org.symphonyoss.helpdesk.models.permissions.IsMember;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class CallResponseListener extends AiCommandListener {
    private Call call;

    public CallResponseListener(SymphonyClient symClient, Call call) {
        super(symClient);
        this.call = call;
        init();
    }

    private void init() {
        AiCommand exit = new AiCommand("Exit", 0);
        exit.addAction(new ExitAction(call));

        AiCommand sendInfo = new AiCommand("Room Info", 0);
        sendInfo.addAction(new RoomInfoAction());

        AiCommand sendSummary = new AiCommand("Help Summary", 0);
        sendSummary.addAction(new HelpSummaryAction());
        sendSummary.addPermission(new IsMember());

        getActiveCommands().add(sendInfo);
        getActiveCommands().add(sendSummary);
        getActiveCommands().add(exit);
    }
}
