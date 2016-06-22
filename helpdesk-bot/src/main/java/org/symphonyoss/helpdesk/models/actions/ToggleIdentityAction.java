package org.symphonyoss.helpdesk.models.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/15/16.
 * An AiAction that allows a member to toggle the visibility of his identity.
 */
public class ToggleIdentityAction implements AiAction {
    private final Logger logger = LoggerFactory.getLogger(ToggleIdentityAction.class);
    /**
     * Find member by message from id.
     * Set identity to the opposite of the user's current identity preference.
     * Write member.
     *
     * @param mlMessageParser   the parser contains the input in ML
     * @param message   the received message
     * @param command   the command that triggered this action
     * @return   the sequence of responses generated from this action
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();

        Member member = MemberCache.getMember(message);
        if(member != null) {

            member.setHideIdentity(!member.isHideIdentity());
            if (member.isHideIdentity()) {

                userIdList.add(message.getFromUserId());
                aiResponseSequence.addResponse(new AiResponse(HelpBotConstants.HIDE_IDENTITY,
                        MessageSubmission.FormatEnum.TEXT, userIdList));

            } else {

                userIdList.add(message.getFromUserId());
                aiResponseSequence.addResponse(new AiResponse(HelpBotConstants.SHOW_IDENTITY,
                        MessageSubmission.FormatEnum.TEXT, userIdList));

            }

        }else{
            logger.warn("Member {} could not be found. Ignoring action.", message.getFromUserId());
        }

        MemberCache.writeMember(member);

        return aiResponseSequence;
    }



}
