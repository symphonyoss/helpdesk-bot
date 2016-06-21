package org.symphonyoss.helpdesk.listeners.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.DeskUserCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.pod.model.User;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/21/16.
 */
public class CallServiceListener implements ChatServiceListener{
    private final Logger logger = LoggerFactory.getLogger(CallServiceListener.class);
    private Call call;

    public CallServiceListener(){
        this.call = call;
    }

    public void onNewChat(Chat chat) {
        //Not possible so do nothing
    }

    public void onRemovedChat(Chat chat) {
        Set<User> users = chat.getRemoteUsers();
        if (users.size() > 0) {
            User user = users.iterator().next();
            DeskUser deskUser = DeskUserCache.getDeskUser(user.getId().toString());
            if (deskUser.getUserType() == DeskUser.DeskUserType.MEMBER)
                call.exit(MemberCache.getMember(deskUser.getUserID().toString()));
            else if (deskUser.getUserType() == DeskUser.DeskUserType.HELP_CLIENT)
                call.exit(ClientCache.retrieveClient(deskUser.getUserID().toString()));
        }
    }
}
