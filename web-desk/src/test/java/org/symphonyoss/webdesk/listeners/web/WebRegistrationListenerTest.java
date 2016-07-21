package org.symphonyoss.webdesk.listeners.web;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.symphonyoss.webdesk.models.calls.MultiChatWebCall;

import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 7/11/16.
 */
public class WebRegistrationListenerTest extends TestCase {

    static WebRegistrationListener webRegistrationListener = mock(WebRegistrationListener.class);


    public void testOnSessionInit() throws Exception {
        Mockito.doCallRealMethod().when(webRegistrationListener).onSessionInit(null);

        try {
            webRegistrationListener.onSessionInit(null);
        }catch(Exception e){
            fail("Initiate call failed.");
        }

    }

    public void testOnSessionTerminate() throws Exception {
        Mockito.doCallRealMethod().when(webRegistrationListener).onSessionTerminate(null);

        try {
            webRegistrationListener.onSessionTerminate(null);
        }catch(Exception e){
            fail("Initiate call failed.");
        }
    }
}