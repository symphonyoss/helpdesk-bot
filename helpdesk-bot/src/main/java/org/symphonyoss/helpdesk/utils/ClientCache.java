package org.symphonyoss.helpdesk.utils;

import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.User;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class ClientCache {
    public static final ConcurrentHashMap<String, HelpClient> ALL_CLIENTS = new ConcurrentHashMap<String, HelpClient>();

    public static HelpClient addClient(User user) {
        if(user == null)
            return null;

        HelpClient helpClient = new HelpClient(user.getEmailAddress(), user.getId());

        ALL_CLIENTS.put(user.getId().toString(),
                helpClient);
        DeskUserCache.addUser(helpClient);

        return helpClient;
    }

    public static HelpClient retrieveClient(Message message) {
        return ALL_CLIENTS.get(message.getFromUserId().toString());
    }

    public static HelpClient removeClient(User user) {
        HelpClient client = retrieveClient(user);
        ALL_CLIENTS.remove(user.getId().toString());
        DeskUserCache.removeUser(client);

        return client;
    }

    public static HelpClient retrieveClient(User user) {
        return ALL_CLIENTS.get(user.getId().toString());
    }

    public static HelpClient retrieveClient(String userID) {
        return ALL_CLIENTS.get(userID);
    }

    public static boolean hasClient(Long id) {
        if(id == null)
            return false;

        return ALL_CLIENTS.containsKey(id.toString());
    }
}
