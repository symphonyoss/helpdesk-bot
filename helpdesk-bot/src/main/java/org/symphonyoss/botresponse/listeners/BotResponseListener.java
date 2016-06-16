package org.symphonyoss.botresponse.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.botresponse.enums.MLTypes;
import org.symphonyoss.botresponse.models.BotResponse;
import org.symphonyoss.botresponse.models.LastBotResponse;
import org.symphonyoss.botresponse.utils.BotInterpreter;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 6/13/16.
 */

/*
 * BotResponseListener.java
 * A class that listens in on a chat, and determines how to respond
 * appropriately based on all of the active bot responses, and the user's input.
 */
public class BotResponseListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(BotResponseListener.class);
    private final HashSet<BotResponse> activeResponses = new HashSet<BotResponse>();
    private final ConcurrentHashMap<String, LastBotResponse> lastResponse = new ConcurrentHashMap<String, LastBotResponse>();
    private SymphonyClient symClient;

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
        logger.debug("Received message for response.");
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
            if (response.isCommand(chunks, message) && response.userHasPermission(message.getFromUserId().toString())) {
                response.respond(mlMessageParser, message, this);
                lastResponse.put(message.getId(), new LastBotResponse(mlMessageParser, response));
                responded = true;
            } else if (response.isCommand(chunks, message) && !response.userHasPermission(message.getFromUserId().toString()))
                sendNoPermission(message);

        if (!responded
                && !(mlMessageParser.getText().trim().equalsIgnoreCase("Run Last") && lastResponse.get(message.getFromUserId().toString()) != null)
                && !BotInterpreter.interpretable(activeResponses, chunks, HelpBotConstants.CORRECTFACTOR))
            sendUsage(message);
        else if (!responded && !(mlMessageParser.getText().trim().equalsIgnoreCase("Run Last") && lastResponse.get(message.getFromUserId().toString()) != null)) {
            LastBotResponse interpret = BotInterpreter.interpret(activeResponses, chunks, symClient, HelpBotConstants.CORRECTFACTOR);
            Messenger.sendMessage(MLTypes.START_ML + "Did you mean "
                            + MLTypes.START_BOLD + interpret.getMlMessageParser().getText()
                            + MLTypes.END_BOLD + "? (Type " + MLTypes.START_BOLD + "Run Last"
                            + MLTypes.END_BOLD + "to run command)" + MLTypes.END_ML,
                    MessageSubmission.FormatEnum.MESSAGEML, message, symClient);

            lastResponse.put(message.getFromUserId().toString(), interpret);
        } else if (!responded) {
            LastBotResponse lastBotResponse = lastResponse.get(message.getFromUserId().toString());
            lastBotResponse.getBotResponse().respond(lastBotResponse.getMlMessageParser(), message, this);
        }
    }

    private void sendUsage(Message message) {

        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);

        String usage = MLTypes.START_ML + "Sorry...  " + MLTypes.BREAK + MLTypes.START_BOLD
                + "Check the usage:" + MLTypes.END_BOLD + MLTypes.BREAK;
        for (BotResponse response : activeResponses)
            if (response.userHasPermission(message.getFromUserId().toString()))
                usage += response.toMLString();

        usage += MLTypes.END_ML;

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
            logger.error(e.getMessage());
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
