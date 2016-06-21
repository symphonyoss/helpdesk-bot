package org.symphonyoss.test;

import org.junit.Test;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.helpdesk.bots.HelpDeskBot;
import org.symphonyoss.symphony.pod.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Created by nicktarsillo on 6/21/16.
 */
public class HelpBotTest {
    @Test
    public void testOnNewChat() {
        HelpDeskBot bot = mock(HelpDeskBot.class);
        Chat chat = new Chat();
        Set<User> users = new HashSet<User>();
        users.add(null);
        chat.setRemoteUsers(users);
        try {
            bot.onNewChat(chat);
            bot.setupBot();
        } catch (Exception e) {
            e.printStackTrace();
            fail("New failed.");
        }
    }

    @Test
    public void testOnRemovedChat() {
        HelpDeskBot bot = mock(HelpDeskBot.class);
        try {
            bot.onRemovedChat(new Chat());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Remove failed.");
        }
    }
}