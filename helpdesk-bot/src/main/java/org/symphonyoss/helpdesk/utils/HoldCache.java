package org.symphonyoss.helpdesk.utils;

import org.symphonyoss.helpdesk.models.users.HelpClient;

import java.util.ArrayList;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class HoldCache {
    public static final ArrayList<HelpClient> ONHOLD = new ArrayList<HelpClient>();

    public static void putClientOnHold(HelpClient client) {
        ONHOLD.add(client);
    }

    public static HelpClient pickUpNextClient() {
        HelpClient client = ONHOLD.get(0);
        ONHOLD.remove(client);
        return client;
    }

    public static HelpClient pickUpClient(HelpClient client) {
        ONHOLD.remove(client);
        return client;
    }

    public static HelpClient findClientCredentialMatch(String credential) {
        for (HelpClient client : HoldCache.ONHOLD)
            if (credential.equalsIgnoreCase(client.getEmail()) || credential.equalsIgnoreCase(client.getUserID().toString())) {
                return client;
            }

        return null;
    }

    public static String listQueue() {
        String list = "";
        for (HelpClient client : ONHOLD)
            if (client.getEmail() != "" && client.getEmail() != null)
                list += ", " + client.getEmail();
            else
                list += ", " + client.getUserID();
        if (ONHOLD.size() > 0)
            return list.substring(1);
        else
            return list;
    }

    public static boolean hasClient(HelpClient client) {
        return ONHOLD.contains(client);
    }
}
