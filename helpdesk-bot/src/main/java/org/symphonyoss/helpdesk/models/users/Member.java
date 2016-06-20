package org.symphonyoss.helpdesk.models.users;

import org.symphonyoss.helpdesk.enums.DeskUserType;
import org.symphonyoss.helpdesk.listeners.Call;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Member implements DeskUser {
    private Call call;
    private String email;
    private Long userID;
    private boolean onCall;
    private boolean seeCommands = true;
    private boolean busy;
    private boolean hideIdentity;
    private boolean online;

    public Member(String email, Long userID) {
        setEmail(email);
        setUserID(userID);
    }

    public Member(String email, Long userID, boolean seeCommands, boolean hideIdentity) {
        setEmail(email);
        setUserID(userID);
        this.seeCommands = seeCommands;
        this.hideIdentity = hideIdentity;
    }

    public DeskUserType getUserType() {
        return DeskUserType.MEMBER;
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

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public SerializableMember toSerializable() {
        return new SerializableMember(email, userID, seeCommands, hideIdentity);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
