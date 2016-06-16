package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.chat.BotResponseListener;
import org.symphonyoss.helpdesk.models.BotResponse;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientDatabase;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class AddMemberResponse extends BotResponse {

    public AddMemberResponse(String command, int numArguments) {
        super(command, numArguments);
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        String[] chunks = mlMessageParser.getTextChunks();
        String id = String.join(" ", chunks);
        id = id.substring(id.indexOf(getPrefixRequirement(0)));

        try {
            User user = listener.getSymClient().getUsersClient().getUserFromEmail(id);
            if (user != null && !MemberDatabase.MEMBERS.containsKey(user.getId().toString())) {
                Member member = new Member(id,
                        user.getId());
                MemberDatabase.addMember(member);

                HelpClient client = ClientDatabase.removeClient(user);
                Messenger.sendMessage("You have been promoted to member!", MessageSubmission.FormatEnum.TEXT,
                        client.getUserID(), listener.getSymClient());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean userHasPermission(String userID) {
        return MemberDatabase.hasMember(userID)
                && !MemberDatabase.getMember(userID).isOnCall();
    }
}
