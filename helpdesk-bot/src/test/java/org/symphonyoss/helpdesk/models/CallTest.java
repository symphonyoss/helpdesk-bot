package org.symphonyoss.helpdesk.models;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/22/16.
 */
public class CallTest {
    static Call call = mock(Call.class);

    @Test
    public void testInitiateCall() throws Exception {
        Mockito.doCallRealMethod().when(call).initiateCall();

        try{
            call.initiateCall();
        }catch(Exception e){
            fail("init failed");
        }
    }

    @Test
    public void testEnter() throws Exception {
        Mockito.doCallRealMethod().when(call).enter(new HelpClient(new String(), new Long(0)));

        try{
            call.enter(new HelpClient(new String(), new Long(0)));
        }catch(Exception e){
            fail("enter failed");
        }
    }

    @Test
    public void testEnter1() throws Exception {
        Mockito.doCallRealMethod().when(call).enter(new Member(new String(), new Long(0)));

        try{
            call.enter(new Member(new String(), new Long(0)));
        }catch(Exception e){
            fail("enter failed");
        }
    }

    @Test
    public void testExitCall() throws Exception {
        Mockito.doCallRealMethod().when(call).exitCall();

        try{
            call.exitCall();
        }catch(Exception e){
            fail("exit failed");
        }
    }
}