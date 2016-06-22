package org.symphonyoss.helpdesk.bots;

import org.junit.Test;
import org.mockito.Mockito;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nicktarsillo on 6/21/16.
 */
public class HelpDeskBotTest {

    @Test
    public void testSetupBot() throws Exception {
        HelpDeskBot bot = mock(HelpDeskBot.class);
        Mockito.doCallRealMethod().when(bot).setupBot();
        try {
            bot.setupBot();
        }catch(Exception e){
            fail("Setup failed.");
        }
    }

    @Test
    public void testInitConnection() throws Exception {
        HelpDeskBot bot = mock(HelpDeskBot.class);
        Mockito.doCallRealMethod().when(bot).initConnection();
        try {
            bot.initConnection();
        }catch(Exception e){
            fail("init failed.");
        }
    }

    @Test
    public void testOnNewChat() {
        HelpDeskBot bot = mock(HelpDeskBot.class);
        Mockito.doCallRealMethod().when(bot).onNewChat(new Chat());

        Chat chat = new Chat();
        chat.setStream(new Stream());
        Set<User> users = new HashSet<User>();
        users.add(new User());
        chat.setRemoteUsers(users);
        try {
            bot.onNewChat(chat);
        } catch (Exception e) {
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