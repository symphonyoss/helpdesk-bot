package org.symphonyoss.helpdesk.listeners.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.models.calls.Call;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.pod.model.User;

import java.util.Set;

/**
 * Created by nicktarsillo on 6/21/16.
 * Handles removing or adding chats in a call.
 */
public class CallServiceListener implements ChatServiceListener {
    private final Logger logger = LoggerFactory.getLogger(CallServiceListener.class);
    private Call call;

    public CallServiceListener(Call call) {
        this.call = call;
    }

    public void onNewChat(Chat chat) {
        //Not possible so do nothing
    }

    /**
     * On remove chat, exit remote user from call
     * @param chat   the removed chat
     */
    public void onRemovedChat(Chat chat) {


        if (chat != null) {
            Set<User> users = chat.getRemoteUsers();

            if (users != null && users.size() > 0) {
                User user = users.iterator().next();

                DeskUser deskUser = null;
                if(user.getId() != null)
                    deskUser = DeskUserCache.getDeskUser(user.getId().toString());

                if (deskUser != null) {

                    if (deskUser.getUserType() == DeskUser.DeskUserType.MEMBER) {
                        call.exit(MemberCache.getMember(deskUser.getUserID().toString()));
                    }else if (deskUser.getUserType() == DeskUser.DeskUserType.HELP_CLIENT) {
                        call.exit(ClientCache.retrieveClient(deskUser.getUserID().toString()));
                    }

                }else{
                    logger.warn("Could not find desk user {}." +
                            " Ignoring chat removal.", user.getId());
                }

            }
        }


    }



}
