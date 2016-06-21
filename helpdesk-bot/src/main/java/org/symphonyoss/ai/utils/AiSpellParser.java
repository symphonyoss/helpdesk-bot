package org.symphonyoss.ai.utils;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiLastCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;

import java.util.LinkedList;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class AiSpellParser {
    public static boolean canParse(LinkedList<AiCommand> responses, String[] chunks, double closenessFactor) {
        for (AiCommand response : responses) {
            if (chunks.length < response.getNumArguments())
                break;
            int likeness = 0;
            String[] checkCommand = response.getCommand().split("\\s+");
            for (int commandIndex = 0; commandIndex < checkCommand.length && commandIndex < chunks.length; commandIndex++)
                if (isCloseTo(chunks[commandIndex].trim(), checkCommand[commandIndex].trim(), closenessFactor))
                    likeness++;

            int possibleArguments = chunks.length - likeness;
            if (possibleArguments < response.getNumArguments())
                break;

            if (closenessFactor < (((double) likeness) / checkCommand.length))
                return true;
        }
        return false;
    }

    public static AiLastCommand parse(LinkedList<AiCommand> responses, String[] chunks, SymphonyClient symClient, double closenessFactor) {
        for (AiCommand response : responses) {
            if (chunks.length < response.getNumArguments())
                break;
            int likeness = 0;
            String[] checkCommand = response.getCommand().split("\\s+");
            for (int commandIndex = 0; commandIndex < checkCommand.length && commandIndex < chunks.length; commandIndex++)
                if (isCloseTo(chunks[commandIndex].trim(), checkCommand[commandIndex].trim(), closenessFactor))
                    likeness++;

            int possibleArguments = chunks.length - likeness;
            if (possibleArguments < response.getNumArguments())
                break;

            if (closenessFactor < (((double) likeness) / checkCommand.length)) {
                String[] arguments = new String[response.getNumArguments()];
                for (int index = 0; index < response.getNumArguments(); index++)
                    arguments[index] = chunks[(chunks.length - 1) - index];

                String fullCommand = response.getCommand() + " ";
                for (int index = arguments.length - 1; index >= 0; index--)
                    fullCommand += response.getPrefixRequirement((arguments.length - 1) - index) + arguments[index] + " ";
                MlMessageParser mlMessageParser = new MlMessageParser(symClient);
                try {
                    mlMessageParser.parseMessage(MLTypes.START_ML + fullCommand + MLTypes.END_ML);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new AiLastCommand(mlMessageParser, response);
            }
        }
        return null;
    }

    private static boolean isCloseTo(String input1, String input2, double closenessFactor) {
        int likeness = 0;
        String larger = "";
        String smaller = "";

        if (input1.length() > input2.length()) {
            larger = input1;
            smaller = input2;
        } else {
            larger = input2;
            smaller = input1;
        }

        for (int index = 0; index < smaller.length(); index++)
            if (larger.contains(smaller.substring(index, index + 1)))
                likeness++;

        return closenessFactor < (((double) likeness) / larger.length());
    }

}