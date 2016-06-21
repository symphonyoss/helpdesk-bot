package org.symphonyoss.helpdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.utils.CallCache;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class InactivityThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(InactivityThread.class);

    public InactivityThread() {

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(HelpBotConstants.INACTIVITY_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("Inactivity tick triggered. Adding {} miliseconds.", HelpBotConstants.INACTIVITY_INTERVAL);
            CallCache.checkCallInactivity(HelpBotConstants.INACTIVITY_INTERVAL);
        }
    }
}