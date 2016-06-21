package org.symphonyoss.ai.models;

/**
 * Created by nicktarsillo on 6/20/16.
 * An interface that developers can implement to create their own command permissions
 */
public interface AiPermission {
    boolean userHasPermission(Long userID);
}
