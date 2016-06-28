package org.symphonyoss.helpdesk.models.permissions;

import org.symphonyoss.ai.models.AiPermission;
import org.symphonyoss.helpdesk.models.calls.Call;
import org.symphonyoss.helpdesk.utils.DeskUserCache;

/**
 * Created by nicktarsillo on 6/27/16.
 */
public class IsHelpCall implements AiPermission{

    public boolean userHasPermission(Long userID) {

        return DeskUserCache.getDeskUser(userID.toString()).getCall().getCallType()
                == Call.CallTypes.HELP_CALL;

    }
}
