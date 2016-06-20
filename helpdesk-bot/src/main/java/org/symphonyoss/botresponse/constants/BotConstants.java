package org.symphonyoss.botresponse.constants;

import org.symphonyoss.botresponse.enums.MLTypes;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public class BotConstants {
    public static final char COMMAND = '/';

    public static final String SUGGEST = "Did you mean ";
    public static final String USE_SUGGESTION = "? (Type " + MLTypes.START_BOLD + BotConstants.COMMAND + "Run Last"
            + MLTypes.END_BOLD + " to run command)";
    public static final String NO_PERMISSION = "Sorry, you do not have permission to use that command.";
    public static final String NOT_INTERPRETABLE = " is not an interpretable command.";
    public static final String USAGE = "Check the usage:";
}
