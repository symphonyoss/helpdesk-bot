package org.symphonyoss.helpdesk.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by nicktarsillo on 6/13/16.
 */
public abstract class BotResponse {
    public static final HashSet<BotResponse> activeResponses = new HashSet<BotResponse>();

    private Logger logger = LoggerFactory.getLogger(BotResponse.class);

    private String command;
    private int numArguments;
    private String[] prefixRequirements;

    public BotResponse(String command, int numArguments) {
        setCommand(command);
        setNumArguments(numArguments);
        activeResponses.add(this);
    }

    abstract void respond(MlMessageParser mlMessageParser, Message message);

    public boolean isCommand(MlMessageParser mlMessageParser, Message message) {
        String[] chunks = mlMessageParser.getTextChunks();

        if (chunks.length <= numArguments)
            return false;

        if (!chunks[0].trim().equalsIgnoreCase(command))
            return false;

        for (int chunkIndex = 1; chunkIndex <= numArguments; chunkIndex++)
            if (!chunks[chunkIndex].startsWith(prefixRequirements[chunkIndex - 1]))
                return false;

        return true;
    }

    public void setNumArguments(int numArguments) {
        this.numArguments = numArguments;
        String[] resize = new String[numArguments];
        for (int index = 0; index < prefixRequirements.length && index < numArguments; index++) {
            resize[index] = prefixRequirements[index];
            if (resize[index] == null)
                resize[index] = "";
        }
        prefixRequirements = resize;
    }

    public int getNumArguments() {
        return numArguments;
    }

    public void setPrefixRequirement(int argumentIndex, String requirement) {
        if (argumentIndex < numArguments) {
            logger.debug("Could not add prefix requirement {} , not enough arguments.", requirement);
            return;
        }
        prefixRequirements[argumentIndex] = requirement;
    }

    public String getPrefixRequirement(int argumentIndex) {
        if (prefixRequirements.length < argumentIndex)
            return prefixRequirements[argumentIndex];
        else
            return null;
    }


    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}


