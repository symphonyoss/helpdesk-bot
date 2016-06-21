package org.symphonyoss.ai.constants;

/**
 * Created by nicktarsillo on 6/20/16.
 */

/**
 * A set of enums, representing ML strings, that make coding
 * ML types a lot easier.
 */
public enum MLTypes {
    START_ML("<messageML>"),
    END_ML("</messageML>"),
    START_BOLD("<b>"),
    END_BOLD("</b>"),
    BREAK("<br/>");

    private final String text;

    MLTypes(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
