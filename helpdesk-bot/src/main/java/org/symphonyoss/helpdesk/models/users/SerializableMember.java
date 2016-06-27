package org.symphonyoss.helpdesk.models.users;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/17/16.
 * A model that allows the member class to be serialized and written to file
 */
public class SerializableMember {
    private String email;
    private Long userID;
    private Set<String> tags = new LinkedHashSet<String>();

    private boolean seeCommands = true;
    private boolean hideIdentity;

    public SerializableMember(String email, Long userID, boolean seeCommands, boolean hideIdentity, Set<String> tags) {
        this.email = email;
        this.userID = userID;
        this.seeCommands = seeCommands;
        this.hideIdentity = hideIdentity;
        this.tags = tags;
    }

    /**
     * @return   convert back to member
     */
    public Member toMember() {
        return new Member(email, userID, seeCommands, hideIdentity, tags);
    }



}
