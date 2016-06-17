package org.symphonyoss.helpdesk.listeners.presence;

import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCash;
import org.symphonyoss.symphony.pod.model.UserPresence;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class MemberPresenceListener implements PresenceListener {
    public void onUserPresence(UserPresence userPresence) {
        if (MemberCash.MEMBERS.containsKey(userPresence.getUid().toString())) {
            Member member = MemberCash.getMember(userPresence.getUid().toString());
            if (userPresence.getCategory() == UserPresence.CategoryEnum.AVAILABLE)
                member.setBusy(false);
            else
                member.setBusy(true);
        }
    }
}
