package org.symphonyoss.helpdesk.listeners.chat;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class HelpClientListenerTest {
    static HelpClientListener helpClientListenerTest = mock(HelpClientListener.class);

    @Test
    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).onChatMessage(new Message());
        Mockito.doCallRealMethod().when(helpClientListenerTest).onChatMessage(null);

        try {
            helpClientListenerTest.onChatMessage(null);
        }catch(Exception e){
            fail("On chat message null test failed.");
        }

        Message message = new Message();
        try {
            helpClientListenerTest.onChatMessage(message);
        }catch(Exception e){
            fail("On chat message empty message test failed.");
        }

        message.setStream("TEST STREAM");
        try {
            helpClientListenerTest.onChatMessage(message);
        }catch(Exception e){
            fail("On chat message junk stream, empty message test failed.");
        }
    }

    @Test
    public void testListenOn() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).listenOn(null);
        Mockito.doCallRealMethod().when(helpClientListenerTest).listenOn(new Chat());

        try {
            helpClientListenerTest.listenOn(null);
        }catch(Exception e){
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            helpClientListenerTest.listenOn(chat);
        }catch(Exception e){
            e.printStackTrace();
            fail("Listen on empty chat test failed.");
        }

        chat.setStream(new Stream());
        try {
            helpClientListenerTest.listenOn(chat);
        }catch(Exception e){
            fail("Listen on junk stream, empty chat test failed.");
        }
    }

    @Test
    public void testStopListening() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).stopListening(null);
        Mockito.doCallRealMethod().when(helpClientListenerTest).stopListening(new Chat());

        try {
            helpClientListenerTest.stopListening(null);
        }catch(Exception e){
            fail("Listen on null test failed.");
        }

        Chat chat = new Chat();
        try {
            helpClientListenerTest.stopListening(chat);
        }catch(Exception e){
            e.printStackTrace();
            fail("Listen on empty chat test failed.");
        }

        chat.setStream(new Stream());
        try {
            helpClientListenerTest.stopListening(chat);
        }catch(Exception e){
            fail("Listen on junk stream, empty chat test failed.");
        }
    }
}