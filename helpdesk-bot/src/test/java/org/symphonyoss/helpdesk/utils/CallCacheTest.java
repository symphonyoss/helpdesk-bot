package org.symphonyoss.helpdesk.utils;

import org.junit.Test;
import org.symphonyoss.helpdesk.models.HelpBotSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/23/16.
 */
public class CallCacheTest {

    @Test
    public void testNewCall() throws Exception {
        try{
            CallCache.newCall(null, null, new HelpBotSession());
        }catch (Exception e){
            fail("New call test failed");
        }
    }

    @Test
    public void testEndCall() throws Exception {
        try{
            CallCache.endCall(null);
        }catch (Exception e){
            fail("New call test failed");
        }
    }

    @Test
    public void testRemoveCall() throws Exception {
        try{
            CallCache.removeCall(null);
        }catch (Exception e){
            fail("New call test failed");
        }
    }
}