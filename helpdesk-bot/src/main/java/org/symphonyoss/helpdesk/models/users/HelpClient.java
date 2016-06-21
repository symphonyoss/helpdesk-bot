package org.symphonyoss.helpdesk.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.helpdesk.models.Call;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClient implements DeskUser {
    @JsonIgnore
    protected Call call;
    private String email;
    private Long userID;
    private boolean onCall;
    private Set<String> helpRequests = new HashSet<String>();

    public HelpClient(String email, Long userID) {
        setEmail(email);
        setUserID(userID);
    }

    public DeskUserType getUserType() {
        return DeskUserType.HELP_CLIENT;
    }

    public String getHelpSummary() {
        String help;
        if (email != null && !email.equalsIgnoreCase(""))
            help = MLTypes.START_BOLD + "    For " + email + ": "
                    + MLTypes.END_BOLD + MLTypes.BREAK;
        else
            help = MLTypes.START_BOLD + "    For " + userID + ": "
                    + MLTypes.END_BOLD + MLTypes.BREAK;

        for (String line : helpRequests)
            help += "       " + line + MLTypes.BREAK + MLTypes.BREAK;
        return help;
    }

    public Set<String> getHelpRequests() {
        return helpRequests;
    }

    public void setHelpRequests(Set<String> helpRequests) {
        this.helpRequests = helpRequests;
    }

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
