package org.symphonyoss.helpdesk.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nicktarsillo on 6/23/16.
 */
public class HoldCacheTest{

    @Test
    public void testFindClientCredentialMatch() throws Exception {
        try{
            HoldCache.findClientCredentialMatch(null);
        }catch (Exception e){
            fail("New find test failed");
        }
    }
}