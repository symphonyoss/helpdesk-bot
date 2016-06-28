package org.symphonyoss.helpdesk.models;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.helpdesk.models.calls.CallResponder;
import org.symphonyoss.helpdesk.models.calls.HelpCallResponder;
import org.symphonyoss.symphony.agent.model.Message;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallResponderTest {
    static HelpCallResponder callResponder = mock(HelpCallResponder.class);

    @Test
    public void testSendRoomInfo() throws Exception {
        Mockito.doCallRealMethod().when(callResponder).sendRoomInfo(new Long(0));
        Mockito.doCallRealMethod().when(callResponder).sendRoomInfo(null);

        try{
            callResponder.sendRoomInfo(null);
        }catch(Exception e){
            fail("send room info failed");
        }

        try{
            callResponder.sendRoomInfo(new Long(0));
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