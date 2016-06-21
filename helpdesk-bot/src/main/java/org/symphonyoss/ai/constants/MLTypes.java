package org.symphonyoss.ai.constants;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public enum MLTypes {
    START_ML("<messageML>"),
    END_ML("</messageML>"),
    START_BOLD("<b>"),
    END_BOLD("</b>"),
    BREAK("<br/>");

    private final String text;

    private MLTypes(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
