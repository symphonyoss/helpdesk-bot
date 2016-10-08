package org.symphonyoss.roomdesk.listeners.chat;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.utils.Messenger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.roomdesk.config.RoomBotConfig;
import org.symphonyoss.roomdesk.models.users.DeskUser;
import org.symphonyoss.roomdesk.models.users.Member;
import org.symphonyoss.roomdesk.utils.DeskUserCache;
import org.symphonyoss.symphony.clients.model.SymMessage;

import org.symphonyoss.symphony.clients.model.SymUser;

/**
 * Created by nicktarsillo on 7/7/16.
 */
public class MemberAliasListener implements ChatListener {
    private SymphonyClient symClient;

    public MemberAliasListener(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    /**
     * Listens on one to one chats. If a message event is triggered, the bot will
     * send a message into the user's current room, using an specified alias
     *
     * @param message
     */
    public void onChatMessage(SymMessage message) {
        if (message == null
                || message.getStreamId() == null
                || AiCommandListener.isCommand(message, symClient))
            return;

        try {
            RoomService roomService  = new RoomService(symClient);



            SymUser user = symClient.getUsersClient().getUserFromId(message.getFromUserId());
            if (DeskUserCache.getDeskUser(message.getFromUserId().toString()).getUserType() != DeskUser.DeskUserType.MEMBER)
                return;

            Member member = (Member) DeskUserCache.getDeskUser(message.getFromUserId().toString());

            if (member.isUseAlias()) {


                if (member.isOnCall()) {

                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + member.getAlias() + ": " + MLTypes.END_BOLD
                                    + message.getMessage().substring(MLTypes.START_ML.toString().length()),
                            SymMessage.Format.MESSAGEML,
                            member.getCall().getCallChat(),
                            symClient);

                } else {


                    Chat chat = symClient.getChatService().getChatByStream(System.getProperty(RoomBotConfig.MEMBER_CHAT_STREAM));

                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + member.getAlias() + ": " + MLTypes.END_BOLD
                                    + message.getMessage().substring(MLTypes.START_ML.toString().length()),
                            SymMessage.Format.MESSAGEML, chat, symClient);

                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listenOn(Chat chat) {
        if (chat != null && chat.getRemoteUsers().size() == 1) {
            chat.registerListener(this);
        }
    }

    public void stopListenung(Chat chat) {
        if (chat != null) {
            chat.removeListener(this);
        }
    }
}
