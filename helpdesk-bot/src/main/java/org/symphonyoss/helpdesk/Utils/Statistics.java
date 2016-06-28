package org.symphonyoss.helpdesk.utils;

import sun.security.krb5.internal.crypto.Des;

/**
 * Created by nicktarsillo on 6/28/16.
 */
public class Statistics {
    public static int getNumClients(){
        return ClientCache.ALL_CLIENTS.size();
    }

    public static int getNumMembers(){
        return MemberCache.MEMBERS.size();
    }

    public static int getNumOnlineMembers(){
        return MemberCache.getOnlineMembers().size();
    }

    public static int getNumHolds(){
        return HoldCache.ONHOLD.size();
    }

    public static int getNumDeskUsers(){
        return DeskUserCache.ALL_USERS.size();
    }

    public static int getNumCalls(){
        return CallCache.ACTIVECALLS.size();
    }
}
