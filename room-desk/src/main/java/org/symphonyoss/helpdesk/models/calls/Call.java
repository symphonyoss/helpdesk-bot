package org.symphonyoss.helpdesk.models.calls;

import org.symphonyoss.helpdesk.listeners.service.CallServiceListener;

/**
 * Created by nicktarsillo on 7/5/16.
 */
public abstract class Call {
    protected CallServiceListener callServiceListener;

    public Call(){
        callServiceListener = new CallServiceListener(this);
    }

    public enum CallTypes{HELP_CALL}

    public abstract void initiateCall();
    public abstract void endCall();
    public abstract CallTypes getCallType();


}
