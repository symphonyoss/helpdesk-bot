package org.symphonyoss.helpdesk.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.symphonyoss.helpdesk.enums.DeskUserType;
import org.symphonyoss.helpdesk.listeners.Call;

/**
 * Created by nicktarsillo on 6/16/16.
 */
public interface DeskUser {
    @JsonIgnore
    Call getCall();

    void setCall(Call call);

    Long getUserID();

    void setUserID(Long userID);

    String getEmail();

    void setEmail(String userID);

    boolean isOnCall();

    void setOnCall(boolean onCall);

    @JsonIgnore
    DeskUserType getUserType();
}
