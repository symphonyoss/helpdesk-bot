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
 * A class that listens in on a chat, and determines if the user's input
 * matches a command.
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
     * When a chat message is received, check if it starts with
     * the command char. If it does, process message.
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
        }
    }

    /**
     * Check to see if the message matches any of the commands.
     * If it matches, do actions and received responses.
     * If it doesn't check if the ai can suggest a command from the unmatched command.
     * If it can suggest, then suggest the command and save the suggested command as the last command.
     * If it can't suggest and the sent command does not match run last command, send usage
     * If it does equal run last command, run the last command
     * @param mlMessageParser   the parser containing the received input in ML
     * @param chunks   the received input in text chunks
     * @param message   the received message
     */
    private void processMessage(MlMessageParser mlMessageParser, String[] chunks, Message message) {
        boolean responded = false;
        for (AiCommand command : activeCommands)
            if (command.isCommand(chunks) && command.userIsPermitted(message.getFromUserId())) {
                aiResponder.respondWith(command.getResponses(mlMessageParser, message));
                lastResponse.put(message.getId(), new AiLastCommand(mlMessageParser, command));
                responded = true;
            } else if (command.isCommand(chunks)) {
                aiResponder.sendNoPermission(message);
                return;
            }

        if (!responded
                && !equalsRunLastCommand(mlMessageParser, message)
                && !canSuggest(chunks)) {
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

    /**
     * Determines if the given input matches the run last command
     * @param mlMessageParser   the parser that contains the input in ML
     * @param message   the received message
     * @return  if the input matches the run last command
     */
    private boolean equalsRunLastCommand(MlMessageParser mlMessageParser, Message message) {
        return (mlMessageParser.getText().trim().equalsIgnoreCase(AiConstants.RUN_LAST_COMMAND))
                && lastResponse.get(message.getFromUserId().toString()) != null;
    }

    /**
     * Determines if the ai can suggest a command based on the input
     * @param chunks  the text input
     * @return if the ai can suggest a command
     */
    private boolean canSuggest(String[] chunks) {
        return AiSpellParser.canParse(activeCommands, chunks, HelpBotConstants.CORRECTFACTOR);
    }

    /**
     * Determines if the message was pushed, due to registering a new chat listener
     * @param message   the message
     * @return if the message was pushed
     */
    private boolean isPushMessage(Message message) {
        return (entered.get(message.getStream()) == null
                || !entered.get(message.getStream()));
    }

    /**
     * A method that allows other classes to determine if a given message
     * matches a command in this command listener
     * @param message   the message
     * @return if the message is a command
     */
    public boolean isCommand(Message message) {
        logger.debug("Received message for response.");
        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());

            String[] chunks = mlMessageParser.getTextChunks();

            return chunks[0].charAt(0) == AiConstants.COMMAND;
        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
        }
        return false;
    }

    /**
     * Registers this listener to a given chat appropriately.
     * @param chat    The chat to listen on
     */
    public void listenOn(Chat chat) {
        chat.registerListener(this);
        entered.put(chat.getStream().getId(), true);
    }

    /**
     * Removes this listener from the provided chat appropriately
     * @param chat   The chat to listen on
     */
    public void stopListening(Chat chat) {
        chat.removeListener(this);
        entered.put(chat.getStream().getId(), false);
    }

    /**
     * Determines if push commands should be ignored or not
     * @return   if the push command should be ignored
     */
    public boolean isPushCommands() {
        return pushCommands;
    }

    public void setPushCommands(boolean pushCommands) {
        this.pushCommands = pushCommands;
    }

    public LinkedList<AiCommand> getActiveCommands() {
        return activeCommands;
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public AiResponder getResponder() {
        return aiResponder;
    }

    public void setResponder(AiResponder responder) {
        this.aiResponder = responder;
    }
}
