package org.symphonyoss.helpdesk.listeners.chat;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.bots.HelpDeskBot;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallChatListenerTest {
    static CallChatListener listener = mock(CallChatListener.class);

    @Test
    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(listener).onChatMessage(new Message());
        Mockito.doCallRealMethod().when(listener).onChatMessage(null);
        Message message = new Message();

        try {
            listener.onChatMessage(null);
        }catch(Exception e){
            fail("On chat message null test failed.");
        }

        try {
            listener.onChatMessage(message);
        }catch(Exception e){
            fail("On chat message empty message test failed.");
        }

        message.setStream("TEST STREAM");
        try {
            listener.onChatMessage(message);
        }catch(Exception e){
            fail("On chat message junk stream, empty message test failed.");
        }
    }

    @Test
    public void testListenOn() throws Exception {
        Mockito.doCallRealMethod().when(listener).listenOn(null);
        Mockito.doCallRealMethod().when(listener).listenOn(new Chat());

        try {
            listener.listenOn(null);
        }catch(Exception e){
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            listener.listenOn(chat);
        }catch(Exception e){
            e.printStackTrace();
            fail("Listen on empty chat test failed.");
        }

        chat.setStream(new Stream());
        try {
            listener.listenOn(chat);
        }catch(Exception e){
            fail("Listen on junk stream, empty chat test failed.");
        }
    }

    @Test
    public void testStopListening() throws Exception {
        Mockito.doCallRealMethod().when(listener).stopListening(null);
        Mockito.doCallRealMethod().when(listener).stopListening(new Chat());

        try {
            listener.stopListening(null);
        }catch(Exception e){
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            listener.stopListening(chat);
        }catch(Exception e){
            e.printStackTrace();
            fail("Listen on empty chat test failed.");
        }

        chat.setStream(new Stream());
        try {
            listener.stopListening(chat);
        }catch(Exception e){
            fail("Listen on junk stream, empty chat test failed.");
        }
    }
}