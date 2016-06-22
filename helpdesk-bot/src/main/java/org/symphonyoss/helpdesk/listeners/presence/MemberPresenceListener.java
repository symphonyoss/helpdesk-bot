package org.symphonyoss.helpdesk.listeners.presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.services.PresenceListener;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.pod.model.UserPresence;

/**
 * Created by nicktarsillo on 6/15/16.
 * A presence listener that checks if a member is busy or not.
 * If the user is busy, stop sending help requests.
 */
public class MemberPresenceListener implements PresenceListener {
    private final Logger logger = LoggerFactory.getLogger(MemberPresenceListener.class);

    public void onUserPresence(UserPresence userPresence) {
        if(userPresence == null
                || userPresence.getUid() == null){

                if(logger != null)
                    logger.warn("Presence listener received null value from presence {}." +
                            " Ignored message.", userPresence);

            return;
        }

        if (MemberCache.MEMBERS.containsKey(userPresence.getUid().toString())) {

            Member member = MemberCache.getMember(userPresence.getUid().toString());

            if (userPresence.getCategory() == UserPresence.CategoryEnum.AVAILABLE) {
                member.setBusy(false);
            }else {
                member.setBusy(true);
            }

        }
    }



}
