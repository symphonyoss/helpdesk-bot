package org.symphonyoss.helpdesk.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.responses.BotResponse;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.HashSet;

/**
 * Created by nicktarsillo on 6/13/16.
 */

/*
 * BotResponseListener.java
 * A class that listens in on a chat, and determines how to respond
 * appropriately based on all of the active bot responses, and the user's input.
 */
public class BotResponseListener implements ChatListener {
    private Logger logger = LoggerFactory.getLogger(BotResponseListener.class);
    private SymphonyClient symClient;
    private HashSet<BotResponse> activeResponses = new HashSet<BotResponse>();

    public BotResponseListener(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    /**
     * When a chat message is received, decode the user message
     * and compare the user's input to all registered bot response commands.
     * If a match is found, respond to the user. If no match is found, send usage.
     * <p>
     * <p>
     *
     * @param message the received message
     *                </p>
     */

    public void onChatMessage(Message message) {
        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());
        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }

        String[] chunks = mlMessageParser.getTextChunks();
        boolean responded = false;
        for (BotResponse response : activeResponses)
            if (response.isCommand(chunks, message) && response.userHasPermission(message.getFromUserId())) {
                response.respond(mlMessageParser, message, this);
                responded = true;
            } else if (response.isCommand(chunks, message) && !response.userHasPermission(message.getFromUserId()))
                sendNoPermission(message);


        if (!responded)
            sendUsage(message);
    }

    private void sendUsage(Message message) {

        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);

        String usage = "<messageML>Sorry...  <br/><b>Check the usage:</b><br/>";
        for (BotResponse response : activeResponses)
            if (response.userHasPermission(message.getFromUserId()))
                usage += response.toMLString();

        usage += "</messageML>";

        Messenger.sendMessage(usage, MessageSubmission.FormatEnum.MESSAGEML, message, symClient);
    }

    private void sendNoPermission(Message message) {
        Messenger.sendMessage("Sorry, you cannot use that command.",
                MessageSubmission.FormatEnum.TEXT, message, symClient);
    }

    public String getEmail(long userID) {
        try {
            return symClient.getUsersClient().getUserFromId(userID).getEmailAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashSet<BotResponse> getActiveResponses() {
        return activeResponses;
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }
}
