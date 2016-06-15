package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.listeners.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class AcceptHelpResponse extends BotResponse {
    private HelpClientListener helpListener;

    public AcceptHelpResponse(String command, int numArguments, HelpClientListener helpListener) {
        super(command, numArguments);
        this.helpListener = helpListener;
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        String[] chunks = mlMessageParser.getTextChunks();

        if (chunks.length > getCommand().split(" ").length) {
            String id = mlMessageParser.getHtmlStartingFromText(getPrefixRequirement(0));

            Member member = MemberDatabase.members.get(listener.getEmail(message.getFromUserId()));
            HelpClient helpClient = null;
            for (HelpClient client : HelpBotConstants.ONHOLD)
                if (id.equalsIgnoreCase(client.getEmail()) || id.equalsIgnoreCase(client.getUserID().toString())) {
                    helpClient = client;
                    break;
                }

            if (helpClient != null) {
                HelpBotConstants.ONHOLD.remove(helpClient);
                Call newCall = new Call(member, helpClient, listener, helpListener);
                newCall.enterCall();

                Messenger.sendMessage("<messageML>You have been connected with user" + id + ".</br> </br>" + helpClient.getHelpSummary() + "</messageML>",
                        MessageSubmission.FormatEnum.MESSAGEML, message.getFromUserId(), listener.getSymClient());
                if (member.isHideIdentity()) {
                    Messenger.sendMessage("<messageML>You have been connected with a help member. " + ".</br> </br>" + helpClient.getHelpSummary() + "</messageML>",
                            MessageSubmission.FormatEnum.MESSAGEML, helpClient.getUserID(), listener.getSymClient());
                } else {
                    Messenger.sendMessage("<messageML>You have been connected with member " + member.getEmail() + ".</br> </br></messageML>" + helpClient.getHelpSummary(),
                            MessageSubmission.FormatEnum.MESSAGEML, helpClient.getUserID(), listener.getSymClient());
                }
            } else
                Messenger.sendMessage(id + " does not exist, or has not requested help.",
                        MessageSubmission.FormatEnum.TEXT, message.getFromUserId(), listener.getSymClient());
        } else {
            Member member = MemberDatabase.members.get(listener.getEmail(message.getFromUserId()));
            if (HelpBotConstants.ONHOLD.size() > 0) {
                HelpClient helpClient = HelpBotConstants.ONHOLD.remove(0);
            } else
                Messenger.sendMessage("There are no users that need help currently.",
                        MessageSubmission.FormatEnum.TEXT, message.getFromUserId(), listener.getSymClient());

        }
    }

    @Override
    public boolean userHasPermission(long userid) {
        if (MemberDatabase.members.containsKey("" + userid)
                && !MemberDatabase.members.get("" + userid).isOnCall())
            return true;
        return false;
    }
}
