package org.symphonyoss.helpdesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCash {
    public static final ConcurrentLinkedQueue<Call> ACTIVECALLS = new ConcurrentLinkedQueue<Call>();
    private static final Logger logger = LoggerFactory.getLogger(HoldCash.class);

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

    public static void checkCallInactivity(int milliSeconds) {
        HashSet<Call> endCalls = new HashSet<Call>();
        for (Call call : ACTIVECALLS) {
            call.setInactivityTime(call.getInactivityTime() + milliSeconds);
            if (call.getInactivityTime() > HelpBotConstants.MAX_INACTIVITY)
                endCalls.add(call);
        }

        for (Call call : endCalls) {
            endCall(call);
            logger.debug("Removed call due to inactivity.");
        }
    }
}
