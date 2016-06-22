package org.symphonyoss.helpdesk.listeners.presence;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.UserPresence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class MemberPresenceListenerTest {

    @Test
    public void testOnUserPresence() throws Exception {
        MemberPresenceListener memberPresenceListener = mock(MemberPresenceListener.class);
        Mockito.doCallRealMethod().when(memberPresenceListener).onUserPresence(null);
        Mockito.doCallRealMethod().when(memberPresenceListener).onUserPresence(new UserPresence());

        try {
            memberPresenceListener.onUserPresence(null);
        }catch(Exception e){
            fail("Listen on null test failed.");
        }

        UserPresence pres = new UserPresence();
        try {
            memberPresenceListener.onUserPresence(pres);
        }catch(Exception e){
            e.printStackTrace();
            fail("Listen on empty chat test failed.");
        }

        pres.setUid(new Long(21123));
        try {
            memberPresenceListener.onUserPresence(pres);
        }catch(Exception e){
            fail("Listen on junk stream, empty chat test failed.");
        }
    }


}