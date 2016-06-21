package org.symphonyoss.helpdesk.models.permissions;

import org.symphonyoss.ai.models.AiPermission;
import org.symphonyoss.helpdesk.utils.DeskUserCache;

/**
 * Created by nicktarsillo on 6/20/16.
 * A permission, that denies access to those who are in a call
 */
public class OffCall implements AiPermission {
    public boolean userHasPermission(Long userID) {
        return !DeskUserCache.getDeskUser(userID.toString()).isOnCall();
    }
}
