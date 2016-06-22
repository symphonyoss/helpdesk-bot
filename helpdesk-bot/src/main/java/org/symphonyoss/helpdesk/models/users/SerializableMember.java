package org.symphonyoss.helpdesk.models.users;

/**
 * Created by nicktarsillo on 6/17/16.
 * A model that allows the member class to be serialized and written to file
 */
public class SerializableMember {
    private String email;
    private Long userID;
    private boolean seeCommands = true;
    private boolean hideIdentity;

    public SerializableMember(String email, Long userID, boolean seeCommands, boolean hideIdentity) {
        this.email = email;
        this.userID = userID;
        this.seeCommands = seeCommands;
        this.hideIdentity = hideIdentity;
    }

    /**
     * @return   convert back to member
     */
    public Member toMember() {
        return new Member(email, userID, seeCommands, hideIdentity);
    }



}
