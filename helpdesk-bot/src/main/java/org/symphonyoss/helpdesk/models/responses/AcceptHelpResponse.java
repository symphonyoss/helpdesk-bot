package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.HelpDesk;
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
            String id = String.join(" ", chunks);
            id = id.substring(id.indexOf(getPrefixRequirement(0)));

            Member member = MemberDatabase.getMember(message);
            HelpClient helpClient = null;
            for (HelpClient client : HelpDesk.ONHOLD)
                if (id.equalsIgnoreCase(client.getEmail()) || id.equalsIgnoreCase(client.getUserID().toString())) {
                    helpClient = client;
                    break;
                }

            if (helpClient != null) {
                HelpDesk.ONHOLD.remove(helpClient);
                HelpDesk.newCall(member, helpClient, listener, helpListener);
            } else
                Messenger.sendMessage(id + " does not exist, or has not requested help.",
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        } else {
            if (HelpDesk.ONHOLD.size() > 0) {
                HelpClient helpClient = HelpDesk.ONHOLD.remove(0);
            } else
                Messenger.sendMessage("There are no users that need help currently.",
                        MessageSubmission.FormatEnum.TEXT, message, listener.getSymClient());
        }
    }

    @Override
    public boolean userHasPermission(long userid) {
        return MemberDatabase.MEMBERS.containsKey("" + userid)
                && !MemberDatabase.getMember(userid).isOnCall();
    }

    public HelpClientListener getHelpListener() {
        return helpListener;
    }

    public void setHelpListener(HelpClientListener helpListener) {
        this.helpListener = helpListener;
    }
}
