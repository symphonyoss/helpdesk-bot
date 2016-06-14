package org.symphonyoss.helpdesk.models;

import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/13/16.
 */

/**
 * HelloResponse.java
 *
 * Just a little test for the response system.
 */
public class HelloResponse extends BotResponse {
    private SymphonyClient symClient;
    public HelloResponse(String command, int numArguments){
        super(command, numArguments);
        this.symClient = symClient;
    }

    @Override
    public void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener) {
        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);
        aMessage.setMessage("Hello World! I am a bot!");

        listener.sendMessage(message, aMessage);
    }
}
