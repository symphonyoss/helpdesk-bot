package org.symphonyoss.helpdesk.models.responses;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCash;
import org.symphonyoss.helpdesk.utils.MemberCash;
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
        String email = String.join(" ", chunks);
        email = email.substring(email.indexOf(getPrefixRequirement(0)) + 1);

        try {
            User user = listener.getSymClient().getUsersClient().getUserFromEmail(email);
            if (user != null && !MemberCash.hasMember(user.getId().toString())) {
                Member member = new Member(email,
                        user.getId());
                MemberCash.addMember(member);

                HelpClient client = ClientCash.removeClient(user);
                Messenger.sendMessage("You have been promoted to member!", MessageSubmission.FormatEnum.TEXT,
                        user.getId(), listener.getSymClient());
                Messenger.sendMessage("You have promoted " + email + " to member!", MessageSubmission.FormatEnum.TEXT,
                        message, listener.getSymClient());

                UserIdList list = new UserIdList();
                list.add(user.getId());
                Chat chat = listener.getSymClient().getChatService().getChatByStream(
                        listener.getSymClient().getStreamsClient().getStream(list).getId());
                helpClientListener.stopListening(chat);
                chat.registerListener(listener);
            } else {
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
        return MemberCash.hasMember(userID)
                && !MemberCash.getMember(userID).isOnCall();
    }
}
