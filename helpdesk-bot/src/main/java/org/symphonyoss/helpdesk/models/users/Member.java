package org.symphonyoss.helpdesk.models.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Member {
    private String email;
    private Long userID;
    private boolean seeCommands;
    private boolean hideIdentity;
    @JsonIgnore
    private boolean onCall = false;

    public Member(String email, Long userID) {
        this.email = email;
        this.userID = userID;
    }

    @JsonCreator
    public Member(String email, Long userID, boolean seeCommands, boolean hideIdentity) {
        this.email = email;
        this.userID = userID;
        this.seeCommands = seeCommands;
        this.hideIdentity = hideIdentity;
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

    public boolean isSeeCommands() {
        return seeCommands;
    }

    public void setSeeCommands(boolean seeCommands) {
        this.seeCommands = seeCommands;
    }

    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    public boolean isHideIdentity() {
        return hideIdentity;
    }

    public void setHideIdentity(boolean hideIdentity) {
        this.hideIdentity = hideIdentity;
    }
}
