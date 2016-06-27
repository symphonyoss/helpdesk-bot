package org.symphonyoss.ai.listeners;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/24/16.
 */
public class AiCommandListenerTest {

    @Test
    public void testOnChatMessage() throws Exception {
        AiCommandListener commandListener = mock(AiCommandListener.class);
        Mockito.doCallRealMethod().when(commandListener).onChatMessage(null);
        try{
            commandListener.onChatMessage(null);
        }catch(Exception e){
            fail("On chat ai test has failed.");
        }
    }
}