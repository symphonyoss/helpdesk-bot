package org.symphonyoss.helpdesk.models.users;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClient {
    private String email;
    private Long userID;
    private boolean onCall;
    private Set<String> helpRequests = new HashSet<String>();

    public HelpClient(String email, Long userID){this.email = email; this.userID = userID;}

    public String getHelpSummary(){
        String help = "<b>Help Request Summary:</b> <br>";
        for(String line: helpRequests)
            help += line + "</br> </br>";
        return help;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    public Set<String> getHelpRequests() {
        return helpRequests;
    }

    public void setHelpRequests(Set<String> helpRequests) {
        this.helpRequests = helpRequests;
    }
}
