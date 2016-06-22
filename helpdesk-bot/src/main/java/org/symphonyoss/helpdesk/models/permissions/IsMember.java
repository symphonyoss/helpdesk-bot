package org.symphonyoss.helpdesk.models.permissions;

import org.symphonyoss.ai.models.AiPermission;
import org.symphonyoss.helpdesk.utils.MemberCache;

/**
 * Created by nicktarsillo on 6/20/16.
 * A permission, that denies access to those who aren't members.
 */
public class IsMember implements AiPermission {

    public boolean userHasPermission(Long userID) {
        return MemberCache.hasMember(userID.toString());
    }



}
