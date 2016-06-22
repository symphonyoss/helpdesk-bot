package org.symphonyoss.helpdesk.models.actions;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class AcceptHelpActionTest {

    @Test
    public void testRespond() throws Exception {
        AcceptHelpAction action = mock(AcceptHelpAction.class);
        Mockito.doCallRealMethod().when(action).respond(new MlMessageParser(), null, null);
        Mockito.doCallRealMethod().when(action).respond(new MlMessageParser(), new Message(), new AiCommand("", 0));


        try{
            action.respond(new MlMessageParser(), null, null);
        }catch (Exception e){
            fail("Action failed.");
        }

        try{
            action.respond(new MlMessageParser(), new Message(), new AiCommand("", 0));
        }catch (Exception e){
            fail("Action failed.");
        }
    }
}