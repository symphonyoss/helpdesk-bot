package org.symphonyoss.helpdesk.models.users;

/**
 * Created by nicktarsillo on 6/17/16.
 */
public class MemberWrapper {
    private String email;
    private Long userID;
    private boolean seeCommands = true;
    private boolean hideIdentity;

    public MemberWrapper(String email, Long userID, boolean seeCommands, boolean hideIdentity) {
        this.email = email;
        this.userID = userID;
        this.seeCommands = seeCommands;
        this.hideIdentity = hideIdentity;
    }

    public Member toMember() {
        return new Member(email, userID, seeCommands, hideIdentity);
    }
}
