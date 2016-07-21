package org.symphonyoss.webdesk.listeners.chat;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.webdesk.listeners.chat.TranscriptListener;

import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 7/11/16.
 */
public class TranscriptListenerTest extends TestCase {
    static TranscriptListener transcriptListener = mock(TranscriptListener.class);

    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(transcriptListener).onChatMessage(null);
        Mockito.doCallRealMethod().when(transcriptListener).onChatMessage(new Message());

        try{
            transcriptListener.onChatMessage(null);
        }catch(Exception e){
            fail("Transcript test failed.");
        }

        try{
            transcriptListener.onChatMessage(new Message());
        }catch(Exception e){
            fail("Transcript test failed.");
        }
    }

    public void testWrite() throws Exception {
        Mockito.doCallRealMethod().when(transcriptListener).write(null);

        try{
            transcriptListener.onChatMessage(null);
        }catch(Exception e){
            fail("Transcript test failed.");
        }
    }
}