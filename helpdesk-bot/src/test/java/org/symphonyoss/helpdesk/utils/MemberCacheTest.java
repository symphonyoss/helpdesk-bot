package org.symphonyoss.helpdesk.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nicktarsillo on 6/23/16.
 */
public class MemberCacheTest {

    @Test
    public void testWriteMember() throws Exception {
        try{
            MemberCache.writeMember(null);
        }catch (Exception e){
            fail("New write test failed");
        }
    }
}