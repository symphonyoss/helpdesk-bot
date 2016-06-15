package org.symphonyoss.helpdesk.models.responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.listeners.BotResponseListener;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/13/16.
 */

/*
 * BotResponse.java
 * A abstract class, that defines a bot responses.
 * Allows developers to easily create command lines, with multiple arguments and
 * prefixes,for each responses, and compare user input with the command line.
 */
public abstract class BotResponse {
    private final Logger logger = LoggerFactory.getLogger(BotResponse.class);

    private String command;
    private int numArguments;
    private String[] prefixRequirements = new String[0];
    private String[] placeHolders = new String[0];

    public BotResponse(String command, int numArguments) {
        setCommand(command);
        setNumArguments(numArguments);
    }

    /**
     * An abstract method, that can be used in extended classes to create message responses
     * for the bot.
     * <p>
     * <p>
     *
     * @param mlMessageParser the current parser for the message
     * @param message         the message sent from a user
     * @param listener        the listener this responses belongs to
     *                        </p>
     */
    public abstract void respond(MlMessageParser mlMessageParser, Message message, BotResponseListener listener);

    public abstract boolean userHasPermission(long userid);

    /**
     * Checks to see if the user's input fulfills the bot responses command requirements
     * <p>
     * <p>
     *
     * @param chunks  the user's input in chunks
     * @param message the message from the user
     * @return if the user input fulfills the bot responses command requirements
     * </p>
     */
    public boolean isCommand(String[] chunks, Message message) {
        String[] checkCommand = command.split(" ");

        if ((chunks.length - checkCommand.length) + 1 <= numArguments)
            return false;

        for (int commandIndex = 0; commandIndex < checkCommand.length; commandIndex++)
            if (!chunks[commandIndex].trim().equalsIgnoreCase(checkCommand[commandIndex].trim()))
                return false;

        for (int chunkIndex = 1; chunkIndex <= numArguments; chunkIndex++)
            if (!chunks[chunkIndex].startsWith(prefixRequirements[chunkIndex - 1]))
                return false;

        return true;
    }

    /**
     * Creates a usage HTML string, that can be used to instruct users how to use this
     * bot responses command.
     *
     * @return the usage string in HTML
     */
    public String toMLString() {
        String toML = "<b>" + command + "</b> ";
        for (int index = 0; index < numArguments; index++)
            toML += prefixRequirements[index] + placeHolders[index];
        return toML + "<br/>";
    }

    //Private methods
    private void resizePrefixesPlaceholders() {
        String[] resize = new String[numArguments];
        for (int index = 0; index < prefixRequirements.length && index < numArguments; index++) {
            resize[index] = prefixRequirements[index];
            if (resize[index] == null)
                resize[index] = "";
        }
        prefixRequirements = resize;

        resize = new String[numArguments];
        for (int index = 0; index < placeHolders.length && index < numArguments; index++) {
            resize[index] = placeHolders[index];
            if (resize[index] == null)
                resize[index] = "";
        }
        placeHolders = resize;
    }

    public int getNumArguments() {
        return numArguments;
    }

    //Getters and Setters
    public void setNumArguments(int numArguments) {
        this.numArguments = numArguments;
        resizePrefixesPlaceholders();
    }

    public void setPrefixRequirement(int argumentIndex, String requirement) {
        if (argumentIndex > numArguments) {
            logger.debug("Could not add prefix requirement {} , not enough arguments.", requirement);
            return;
        }
        prefixRequirements[argumentIndex] = requirement;
    }

    public void setAllPrefixRequirements(String[] prefixRequirements) {
        this.prefixRequirements = prefixRequirements;
    }

    public String getPrefixRequirement(int argumentIndex) {
        if (prefixRequirements.length > argumentIndex)
            return prefixRequirements[argumentIndex];
        else
            return null;
    }

    public void setPlaceHolder(int argumentIndex, String holder) {
        if (argumentIndex > numArguments) {
            logger.debug("Could not add place holder {} , not enough arguments.", holder);
            return;
        }
        placeHolders[argumentIndex] = holder;
    }

    public void setAllPlaceholders(String[] placeHolders) {
        this.placeHolders = placeHolders;
    }

    public String getPlaceHolder(int argumentIndex) {
        if (placeHolders.length < argumentIndex)
            return placeHolders[argumentIndex];
        else
            return null;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}


