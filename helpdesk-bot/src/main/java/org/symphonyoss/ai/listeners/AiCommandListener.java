package org.symphonyoss.ai.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.AiConstants;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiLastCommand;
import org.symphonyoss.ai.models.AiResponder;
import org.symphonyoss.ai.utils.AiSpellParser;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.symphony.agent.model.Message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicktarsillo on 6/13/16.
 */

/*
 * AiCommandListener.java
 * A class that listens in on aiResponder chat, and determines how to respond
 * appropriately based on all of the active bot actions, and the user's input.
 */
public class AiCommandListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(AiCommandListener.class);
    private final LinkedList<AiCommand> activeCommands = new LinkedList<AiCommand>();
    private final ConcurrentHashMap<String, AiLastCommand> lastResponse = new ConcurrentHashMap<String, AiLastCommand>();
    private SymphonyClient symClient;
    private AiResponder aiResponder;
    private boolean pushCommands;
    private HashMap<String, Boolean> entered = new HashMap<String, Boolean>();

    public AiCommandListener(SymphonyClient symClient) {
        this.symClient = symClient;
        aiResponder = new AiResponder(symClient);
    }

    /**
     * When aiResponder chat message is received, decode the user message
     * and compare the user's input to all registered bot response commands.
     * If aiResponder match is found, respond to the user. If no match is found, send usage.
     * <p>
     * <p>
     *
     * @param message the received message
     *                </p>
     */

    public void onChatMessage(Message message) {
        if (isPushMessage(message)
                && !isPushCommands())
            return;
        logger.debug("Received message for response.");
        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());

            String[] chunks = mlMessageParser.getTextChunks();

            if (chunks[0].charAt(0) == AiConstants.COMMAND) {
                mlMessageParser.parseMessage(message.getMessage().replaceFirst(">" + AiConstants.COMMAND, ">"));
                chunks = mlMessageParser.getTextChunks();
                processMessage(mlMessageParser, chunks, message);
            }
        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }
    }

    private void processMessage(MlMessageParser mlMessageParser, String[] chunks, Message message) {
        boolean responded = false;
        for (AiCommand command : activeCommands)
            if (command.isCommand(chunks, message) && command.userIsPermitted(message.getFromUserId())) {
                aiResponder.respondWith(command.getResponses(mlMessageParser, message));
                lastResponse.put(message.getId(), new AiLastCommand(mlMessageParser, command));
                responded = true;
            } else if (command.isCommand(chunks, message)) {
                aiResponder.sendNoPermission(message);
                return;
            }

        if (!responded
                && !equalsRunLastCommand(mlMessageParser, message)
                && !canGuessCommand(chunks)) {
            aiResponder.sendUsage(message, mlMessageParser, activeCommands);
        } else if (!responded
                && !equalsRunLastCommand(mlMessageParser, message)) {
            AiLastCommand lastCommand = AiSpellParser.parse(activeCommands, chunks, symClient, HelpBotConstants.CORRECTFACTOR);
            aiResponder.sendSuggestionMessage(lastCommand, message);
            lastResponse.put(message.getFromUserId().toString(), lastCommand);
        } else if (!responded) {
            AiLastCommand lastBotResponse = lastResponse.get(message.getFromUserId().toString());
            aiResponder.respondWith(lastBotResponse.getAiCommand().getResponses(lastBotResponse.getMlMessageParser(), message));
        }
    }

    private boolean equalsRunLastCommand(MlMessageParser mlMessageParser, Message message) {
        return (mlMessageParser.getText().trim().equalsIgnoreCase(AiConstants.RUN_LAST_COMMAND))
                && lastResponse.get(message.getFromUserId().toString()) != null;
    }

    private boolean canGuessCommand(String[] chunks) {
        return AiSpellParser.canParse(activeCommands, chunks, HelpBotConstants.CORRECTFACTOR);
    }

    private boolean isPushMessage(Message message) {
        return (entered.get(message.getStream()) == null
                || !entered.get(message.getStream()));
    }

    public boolean isCommand(Message message) {
        logger.debug("Received message for response.");
        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());

            String[] chunks = mlMessageParser.getTextChunks();

            if (chunks[0].charAt(0) == AiConstants.COMMAND) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
        }
        return false;
    }

    public void listenOn(Chat chat) {
        chat.registerListener(this);
        entered.put(chat.getStream().getId(), true);
    }

    public void stopListening(Chat chat) {
        chat.removeListener(this);
        entered.put(chat.getStream().getId(), false);
    }


    public LinkedList<AiCommand> getActiveCommands() {
        return activeCommands;
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public boolean isPushCommands() {
        return pushCommands;
    }

    public void setPushCommands(boolean pushCommands) {
        this.pushCommands = pushCommands;
    }

    public AiResponder getResponder() {
        return aiResponder;
    }

    public void setResponder(AiResponder responder) {
        this.aiResponder = responder;
    }
}
