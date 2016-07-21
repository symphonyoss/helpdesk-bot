package org.symphonyoss.webdesk.utils;

import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.webdesk.models.users.WebClient;
import org.symphonyoss.webservice.models.session.WebSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 7/8/16.
 */
public class WebClientCache {
    public static final ConcurrentHashMap<String, WebClient> ALL_WEB_CLIENTS = new ConcurrentHashMap<String, WebClient>();

    public static WebClient addClient(User user, WebSession webSession) {
        if (user == null)
            return null;

        WebClient webClient = new WebClient(user.getEmailAddress(), user.getId(), webSession);

        ALL_WEB_CLIENTS.put(user.getId().toString(),
                webClient);
        ClientCache.addClient(webClient);

        return webClient;
    }

    public static WebClient retrieveClient(Message message) {
        return ALL_WEB_CLIENTS.get(message.getFromUserId().toString());
    }

    public static WebClient removeClient(User user) {
        WebClient client = retrieveClient(user);
        ALL_WEB_CLIENTS.remove(user.getId().toString());
        DeskUserCache.removeUser(client);
        ClientCache.removeClient(user);

        return client;
    }

    public static WebClient retrieveClient(User user) {
        return ALL_WEB_CLIENTS.get(user.getId().toString());
    }

    public static WebClient retrieveClient(String userID) {
        return ALL_WEB_CLIENTS.get(userID);
    }

    public static boolean hasClient(Long id) {
        if (id == null)
            return false;

        return ALL_WEB_CLIENTS.containsKey(id.toString());
    }
}
