package org.symphonyoss.helpdesk.utils;

import org.symphonyoss.helpdesk.models.users.DeskUser;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class DeskUserCache {
    public static final ConcurrentHashMap<String, DeskUser> ALL_USERS = new ConcurrentHashMap<String, DeskUser>();

    public static void addUser(DeskUser user) {
        ALL_USERS.put(user.getUserID().toString(), user);
    }

    public static void removeUser(DeskUser user) {
        ALL_USERS.put(user.getUserID().toString(), user);
    }

    public static DeskUser getDeskUser(String userID) {
        if (userID != null) {
            return ALL_USERS.get(userID);
        }else {
            return null;
        }
    }
}
