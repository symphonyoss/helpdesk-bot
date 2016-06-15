package org.symphonyoss.helpdesk.listeners;

import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClientListener implements ChatListener {
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public void onChatMessage(Message message) {
        HelpBotConstants.ALLCLIENTS.get(message.getId()).getHelpRequests().add(message.toString());

        for (Member member : MemberDatabase.members.values())
            if (!member.isOnCall() && member.isSeeCommands()) {
                if (HelpBotConstants.ALLCLIENTS.get(message.getId()).getEmail() != null &&
                        HelpBotConstants.ALLCLIENTS.get(message.getId()).getEmail() != "") {
                    Messenger.sendMessage("<messageML><br>" + HelpBotConstants.ALLCLIENTS.get(message.getId()).getEmail() + ":</br> "
                            + message.toString() + "</messageML>", MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);
                } else {
                    Messenger.sendMessage("<messageML><br>" + message.getId() + ":</br> " + message.getMessage() + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, member.getUserID(), symClient);
                }
            }
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
