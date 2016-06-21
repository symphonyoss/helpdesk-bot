package org.symphonyoss.ai.models;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public interface AiPermission {
    boolean userHasPermission(Long userID);
}
