package org.symphonyoss.helpdesk.utils;

import org.junit.Test;
import org.symphonyoss.helpdesk.models.HelpBotSession;

import static org.junit.Assert.*;

/**
 * Created by nicktarsillo on 6/23/16.
 */
public class ClientCacheTest {

    @Test
    public void testAddClient() throws Exception {
        try{
            ClientCache.addClient(null);
        }catch (Exception e){
            fail("New call test failed");
        }
    }

    @Test
    public void testHasClient() throws Exception {
        try{
            ClientCache.hasClient(null);
        }catch (Exception e){
            fail("New call test failed");
        }
    }
}