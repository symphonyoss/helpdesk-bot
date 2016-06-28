package org.symphonyoss.helpdesk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.command.CallCommandListener;
import org.symphonyoss.helpdesk.models.calls.Call;
import org.symphonyoss.helpdesk.models.HelpBotSession;
import org.symphonyoss.helpdesk.models.calls.HelpCall;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class CallCache {
    public static final ConcurrentLinkedQueue<Call> ACTIVECALLS = new ConcurrentLinkedQueue<Call>();
    private static final Logger logger = LoggerFactory.getLogger(CallCache.class);

    public static Call newCall(Member member, HelpClient helpClient, HelpBotSession helpBotSession){
        if(member == null
                || helpClient == null
                || helpBotSession == null) {

            if(logger != null)
                logger.error("Could not create new call. NullPointer!");

            return null;
        }

        Call newCall = new HelpCall(member, helpClient,helpBotSession);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    public static Call newCall(SymphonyClient symClient, boolean isPrivate){
        if(symClient == null) {

            if(logger != null)
                logger.error("Could not create new call. NullPointer!");

            return null;
        }

        Call newCall = new Call(symClient, isPrivate);

        newCall.initiateCall();
        ACTIVECALLS.add(newCall);

        return newCall;

    }

    public static void endCall(Call call) {

        if(call == null)
            return;

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
