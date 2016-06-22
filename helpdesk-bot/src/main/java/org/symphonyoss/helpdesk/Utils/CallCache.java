package org.symphonyoss.helpdesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCache {
    public static final ConcurrentLinkedQueue<Call> ACTIVECALLS = new ConcurrentLinkedQueue<Call>();
    private static final Logger logger = LoggerFactory.getLogger(HoldCache.class);

    public static Call newCall(Member member, HelpClient helpClient, AiCommandListener memberListener, HelpClientListener helpListener, SymphonyClient symphonyClient) {

        Call newCall = new Call(member, helpClient, memberListener, helpListener, symphonyClient);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    public static void endCall(Call call) {

        ACTIVECALLS.remove(call);
        call.exitCall();

    }

    public static void removeCall(Call call){

        ACTIVECALLS.remove(call);

    }

    public static void checkCallInactivity(int milliSeconds) {


        for (Call call : new LinkedList<Call>(ACTIVECALLS)) {
            call.setInactivityTime(call.getInactivityTime() + milliSeconds);

            if (call.getInactivityTime() > HelpBotConstants.MAX_INACTIVITY) {
                endCall(call);
                logger.debug("Removed call due to inactivity.");
            }

        }


    }
}
