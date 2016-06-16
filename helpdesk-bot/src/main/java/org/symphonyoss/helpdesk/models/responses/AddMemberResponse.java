package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientDatabase;
import org.symphonyoss.helpdesk.utils.MemberDatabase;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class AddMemberResponse extends BotResponse {
    private HelpClientListener helpClientListener;

    public AddMemberResponse(String command, int numArguments, HelpClientListener helpClientListener) {
        super(command, numArguments);
        this.helpClientListener = helpClientListener;
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        String[] chunks = mlMessageParser.getTextChunks();
        String id = String.join(" ", chunks);
        id = id.substring(id.indexOf(getPrefixRequirement(0)) + 1);

        try {
            User user = listener.getSymClient().getUsersClient().getUserFromEmail(id);
            if (user != null && !MemberDatabase.MEMBERS.containsKey(user.getId().toString())) {
                Member member = new Member(id,
                        user.getId());
                MemberDatabase.addMember(member);

                HelpClient client = ClientDatabase.removeClient(user);
                Messenger.sendMessage("You have been promoted to member!", MessageSubmission.FormatEnum.TEXT,
                        user.getId(), listener.getSymClient());
                Messenger.sendMessage("You have promoted " + id + " to member!", MessageSubmission.FormatEnum.TEXT,
                        message, listener.getSymClient());

                UserIdList list = new UserIdList();
                list.add(user.getId());
                Chat chat = listener.getSymClient().getChatService().getChatByStream(
                        listener.getSymClient().getStreamsClient().getStream(list).getId());
                chat.removeListener(helpClientListener);
                chat.registerListener(listener);
            }else{
                Messenger.sendMessage("Failed to promote client to member. Either client does not exist " +
                                "or client is already a member.", MessageSubmission.FormatEnum.TEXT,
                       message, listener.getSymClient());
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
