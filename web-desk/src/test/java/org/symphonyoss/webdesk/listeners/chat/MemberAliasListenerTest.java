package org.symphonyoss.webdesk.listeners.chat;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.webdesk.listeners.chat.MemberAliasListener;

import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 7/11/16.
 */
public class MemberAliasListenerTest extends TestCase {
    static MemberAliasListener memberAliasListener = mock(MemberAliasListener.class);

    public void testOnChatMessage() throws Exception {
        Mockito.doCallRealMethod().when(memberAliasListener).onChatMessage(null);
        Mockito.doCallRealMethod().when(memberAliasListener).onChatMessage(new Message());

        try{
            memberAliasListener.onChatMessage(null);
        }catch(Exception e){
            fail("Member listener test failed.");
        }

        try{
            memberAliasListener.onChatMessage(new Message());
        }catch(Exception e){
            fail("Member listener test failed.");
        }
    }
}