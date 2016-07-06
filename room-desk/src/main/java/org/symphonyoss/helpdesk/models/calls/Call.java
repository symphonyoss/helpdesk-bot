package org.symphonyoss.helpdesk.models.calls;

import org.symphonyoss.helpdesk.listeners.service.CallServiceListener;

/**
 * A model that acts a skeleton for a Call
 */
public abstract class Call {
    protected CallServiceListener callServiceListener;

    public Call() {
        callServiceListener = new CallServiceListener(this);
    }

    public abstract void initiateCall();

    public abstract void endCall();

    public abstract CallTypes getCallType();

    public enum CallTypes {HELP_CALL}


}
