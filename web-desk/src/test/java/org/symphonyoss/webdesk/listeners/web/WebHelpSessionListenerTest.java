package org.symphonyoss.webdesk.listeners.web;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.webdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.webservice.models.web.WebMessage;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 7/11/16.
 */
public class WebHelpSessionListenerTest extends TestCase {
    static WebHelpSessionListener helpClientListenerTest = mock(WebHelpSessionListener.class);

    @Test
    public void testOnNewWSMessage() throws Exception {
        Mockito.doCallRealMethod().when(helpClientListenerTest).onNewWSMessage(null);

        try {
            helpClientListenerTest.onNewWSMessage(null);
        } catch (Exception e) {
            fail("On web message null test failed.");
        }
    }
}