package org.symphonyoss.webdesk.models.calls;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 7/21/16.
 */
public class TimerThread extends Thread{
    public static Set<Timer> timerSet = new HashSet<Timer>();

    static{
        TimerThread timerThread = new TimerThread();
        timerThread.start();
    }

    public void run(){


        while(true){

            for(Timer timer: timerSet) {
                timer.setTime(timer.getTime() + 1);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }



}
