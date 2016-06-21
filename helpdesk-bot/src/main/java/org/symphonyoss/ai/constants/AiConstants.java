package org.symphonyoss.ai.constants;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class AiConstants {
    public static final char COMMAND = '/';
    public static final String RUN_LAST_COMMAND = "Run Last";

    public static final String SUGGEST = "Did you mean ";
    public static final String USE_SUGGESTION = "? (Type " + MLTypes.START_BOLD + AiConstants.COMMAND + "Run Last"
            + MLTypes.END_BOLD + " to run command)";
    public static final String NO_PERMISSION = "Sorry, you do not have permission to use that command.";
    public static final String NOT_INTERPRETABLE = " is not a command.";
    public static final String USAGE = "Check the usage:";
}
