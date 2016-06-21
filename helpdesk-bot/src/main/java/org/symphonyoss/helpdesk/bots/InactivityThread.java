package org.symphonyoss.helpdesk.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.utils.CallCache;

/**
 * Created by nicktarsillo on 6/15/16.
 * A thread in charge of adding inactivity time.
 */
public class InactivityThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(InactivityThread.class);
    private boolean stop;

    public InactivityThread() {

    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(HelpBotConstants.INACTIVITY_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("Inactivity tick triggered. Adding {} miliseconds.", HelpBotConstants.INACTIVITY_INTERVAL);
            CallCache.checkCallInactivity(HelpBotConstants.INACTIVITY_INTERVAL);
        }
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
