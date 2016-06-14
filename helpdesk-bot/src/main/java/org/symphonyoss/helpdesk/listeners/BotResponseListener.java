package org.symphonyoss.helpdesk.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.BotResponse;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.Stream;

import java.util.HashSet;

/**
 * Created by nicktarsillo on 6/13/16.
 */

/*
 * BotResponseListener.java
 * A class that listens in on a chat, and determines how to respond
 * appropriately based on all of the active bot responses, and the user's input.
 */
public class BotResponseListener implements ChatListener,ChatServiceListener {
    private Logger logger = LoggerFactory.getLogger(BotResponseListener.class);
    private SymphonyClient symClient;
    private HashSet<BotResponse> activeResponses = new HashSet<BotResponse>();

    public BotResponseListener(SymphonyClient symClient){
        this.symClient = symClient;
    }

    /**
     * When a new chat is created, listen in on the new chat.
     *
     * <p>
     *     @param chat  the newly created chat
     * </p>
     */
    public void onNewChat(Chat chat) {
        chat.registerListener(this);
    }

    /**
     * When a chat is removed, stop listening in on the chat.
     *
     * <p>
     *     @param chat  the removed chat
     * </p>
     */
    public void onRemovedChat(Chat chat) {
        chat.registerListener(this);
    }

    /**
     * When a chat message is received, decode the user message
     * and compare the user's input to all registered bot response commands.
     * If a match is found, respond to the user. If no match is found, send usage.
     *
     * <p>
     *     @param message  the received message
     * </p>
     */
    public void onChatMessage(Message message) {
        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());
        }catch(Exception e){
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }

        String[] chunks = mlMessageParser.getTextChunks();
        boolean responded = false;
        for(BotResponse response: activeResponses)
            if(response.isCommand(chunks, message)) {
                response.respond(mlMessageParser, message, this);
                responded = true;
            }

        if(!responded)
            sendUsage(message);
    }

    public void sendMessage(Message message, MessageSubmission aMessage){
        Stream stream = new Stream();
        stream.setId(message.getStream());
        try {
            symClient.getMessagesClient().sendMessage(stream, aMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUsage(Message message) {

        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);

        String usage = "<messageML>Sorry...  <br/><b>Check the usage:</b><br/>";
        for(BotResponse response: activeResponses)
            usage += response.toMLString();

        aMessage.setMessage(usage);

        sendMessage(message, aMessage);
    }

    public HashSet<BotResponse> getActiveResponses(){
        return activeResponses;
    }
}
