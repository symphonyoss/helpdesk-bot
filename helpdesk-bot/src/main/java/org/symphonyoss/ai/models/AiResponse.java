package org.symphonyoss.ai.models;

import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class AiResponse {
    private String message;
    private MessageSubmission.FormatEnum type;
    private UserIdList toIDs = new UserIdList();

    public AiResponse(String message, MessageSubmission.FormatEnum type, UserIdList userIdList) {
        this.type = type;
        this.message = message;
        this.toIDs = userIdList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageSubmission.FormatEnum getType() {
        return type;
    }

    public void setType(MessageSubmission.FormatEnum type) {
        this.type = type;
    }

    public UserIdList getToIDs() {
        return toIDs;
    }

    public void setToIDs(UserIdList toIDs) {
        this.toIDs = toIDs;
    }
}