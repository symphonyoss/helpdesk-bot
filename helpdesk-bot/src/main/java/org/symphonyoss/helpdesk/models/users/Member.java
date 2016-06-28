package org.symphonyoss.helpdesk.models.users;

import org.symphonyoss.helpdesk.models.calls.Call;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/14/16.
 * A model that represents a member.
 */
public class Member implements DeskUser {
    private Call call;
    private String email;
    private Long userID;
    private Set<String> tags = new LinkedHashSet<String>();
    private boolean onCall;
    private boolean seeHelpRequests = true;
    private boolean busy;
    private boolean hideIdentity;
    private boolean online;

    public Member(String email, Long userID) {
        setEmail(email);
        setUserID(userID);
    }

    public Member(String email, Long userID, boolean seeHelpRequests, boolean hideIdentity, Set<String> tags) {
        setEmail(email);
        setUserID(userID);
        this.seeHelpRequests = seeHelpRequests;
        this.hideIdentity = hideIdentity;
        this.tags = tags;
    }

    /**
     * @return   the type of user (Member)
     */
    public DeskUserType getUserType() {
        return DeskUserType.MEMBER;
    }

    /**
     * Used to get the number of tags that matched with the specified string
     * @param match
     * @return   the number of tags that matched with the specified string
     */
    public int countTagMatches(String match){
        int count = 0;

        for(String tag: getTags()){
            if(match.contains(tag))
                count++;
        }

        return count;
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

    /**
     * @return   if the member should see help requests
     */
    public boolean isSeeHelpRequests() {
        return seeHelpRequests;
    }

    public void setSeeHelpRequests(boolean seeHelpRequests) {
        this.seeHelpRequests = seeHelpRequests;
    }

    /**
     * @return   if the user is in a call
     */
    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    /**
     * @return   if the user wants his identity to be hidden
     */
    public boolean isHideIdentity() {
        return hideIdentity;
    }

    public void setHideIdentity(boolean hideIdentity) {
        this.hideIdentity = hideIdentity;
    }

    public boolean isBusy() {
        return busy;
    }

    /**
     * @param busy   if the user is busy
     */
    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    /**
     * @return   the call the user is currently in
     */
    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public SerializableMember toSerializable() {
        return new SerializableMember(email, userID, seeHelpRequests, hideIdentity, tags);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }


    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
