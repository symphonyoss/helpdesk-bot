package org.symphonyoss.ai.models;

import org.symphonyoss.client.util.MlMessageParser;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class AiLastCommand {
    private MlMessageParser mlMessageParser;
    private AiCommand aiCommand;

    public AiLastCommand(MlMessageParser mlMessageParser, AiCommand aiCommand) {
        this.aiCommand = aiCommand;
        this.mlMessageParser = mlMessageParser;
    }

    public MlMessageParser getMlMessageParser() {
        return mlMessageParser;
    }

    public void setMlMessageParser(MlMessageParser mlMessageParser) {
        this.mlMessageParser = mlMessageParser;
    }

    public AiCommand getAiCommand() {
        return aiCommand;
    }

    public void setAiCommand(AiCommand aiCommand) {
        this.aiCommand = aiCommand;
    }
}
