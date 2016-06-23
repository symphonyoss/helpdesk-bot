package org.symphonyoss.helpdesk.models;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.symphony.agent.model.Message;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallResponderTest {
    static CallResponder callResponder = mock(CallResponder.class);

    @Test
    public void testSendRoomInfo() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendRoomInfo(new Message());
        Mockito.doCallRealMethod().when(callResponder).sendRoomInfo(null);

        try{
            callResponder.sendRoomInfo(null);
        }catch(Exception e){
            fail("send room info failed");
        }

        try{
            callResponder.sendRoomInfo(new Message());
        }catch(Exception e){
            fail("send room info failed");
        }
    }

    @Test
    public void testSendHelpSummary() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendHelpSummary(null);

        try{
            callResponder.sendHelpSummary(null);
        }catch(Exception e){
            fail("send help summary failed");
        }
    }

    @Test
    public void testSendConnectedMessage() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendConnectedMessage(null);

        try{
            callResponder.sendConnectedMessage(null);
        }catch(Exception e){
            fail("send connected failed");
        }
    }

}