package org.symphonyoss.helpdesk.listeners.service;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.pod.model.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallServiceListenerTest {

    @Test
    public void testOnRemovedChat() throws Exception {
        CallServiceListener listener = mock(CallServiceListener.class);
        Mockito.doCallRealMethod().when(listener).onRemovedChat(null);
        Mockito.doCallRealMethod().when(listener).onRemovedChat(new Chat());

        try {
            listener.onRemovedChat(null);
        }catch(Exception e){
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            listener.onRemovedChat(chat);
        }catch(Exception e){
            e.printStackTrace();
            fail("Listen on empty chat test failed.");
        }

        chat.setStream(new Stream());
        try {
            listener.onRemovedChat(chat);
        }catch(Exception e){
            fail("Listen on junk stream, empty chat test failed.");
        }
    }
}