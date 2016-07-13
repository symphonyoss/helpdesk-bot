package org.symphonyoss.webdesk.listeners.chat;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.symphonyoss.symphony.agent.model.Message;

import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 7/11/16.
 */
public class WebCallChatListenerTest extends TestCase {
    static WebCallChatListener webCallChatListener = mock(WebCallChatListener.class);

    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(webCallChatListener).onChatMessage(null);
        Mockito.doCallRealMethod().when(webCallChatListener).onChatMessage(new Message());

        try{
            webCallChatListener.onChatMessage(null);
        }catch(Exception e){
            fail("Chat test failed.");
        }

        try{
            webCallChatListener.onChatMessage(new Message());
        }catch(Exception e){
            fail("Chat test failed.");
        }
    }
}