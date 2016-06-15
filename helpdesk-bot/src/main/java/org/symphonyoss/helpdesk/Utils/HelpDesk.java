package org.symphonyoss.helpdesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.listeners.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class HelpDesk {
    public static final ArrayList<HelpClient> ONHOLD = new ArrayList<HelpClient>();
    public static final ConcurrentLinkedQueue<Call> ACTIVECALLS = new ConcurrentLinkedQueue<Call>();
    public static final ConcurrentHashMap<String, HelpClient> ALLCLIENTS = new ConcurrentHashMap<String, HelpClient>();
    private static final Logger logger = LoggerFactory.getLogger(HelpDesk.class);

    public static Call newCall(Member member, HelpClient helpClient, BotResponseListener listener, HelpClientListener helpListener) {
        Call newCall = new Call(member, helpClient, listener, helpListener);
        newCall.enterCall();
        ACTIVECALLS.add(newCall);
        return newCall;
    }

    public static void endCall(Call call) {
        ACTIVECALLS.remove(call);
        call.exitCall();
    }

    public static void addClient(User user) {
        ALLCLIENTS.put(user.getId().toString(),
                new HelpClient(user.getEmailAddress(), user.getId()));
    }

    public static void checkCallInactivity(int milliSeconds) {
        HashSet<Call> endCalls = new HashSet<Call>();
        for (Call call : ACTIVECALLS) {
            call.setInactivityTime(call.getInactivityTime() + milliSeconds);
            if (call.getInactivityTime() > HelpBotConstants.MAX_INACTIVITY)
                endCalls.add(call);
        }

        for (Call call : endCalls) {
            endCall(call);
            logger.debug("Removed call with client id {} due to inactivity.", call.getClient().getUserID());
        }
    }

    public static HelpClient removeClient(User user) {
        return ALLCLIENTS.remove(user.getId());
    }

    public static HelpClient retrieveClient(User user) {
        return ALLCLIENTS.get(user.getId());
    }

    public static HelpClient retrieveClient(Message message) {
        return ALLCLIENTS.get(message.getFromUserId().toString());
    }
}
