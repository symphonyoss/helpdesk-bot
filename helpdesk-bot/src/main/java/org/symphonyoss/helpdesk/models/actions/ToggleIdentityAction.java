package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseList;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

/**
 * Created by nicktarsillo on 6/15/16.
 */
public class ToggleIdentityAction implements AiAction {
    public AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseList aiResponseList = new AiResponseList();
        UserIdList userIdList = new UserIdList();

        Member member = MemberCache.getMember(message);
        member.setHideIdentity(!member.isHideIdentity());
        if (member.isHideIdentity()) {
            userIdList.add(message.getFromUserId());
            aiResponseList.addResponse(new AiResponse(HelpBotConstants.HIDE_IDENTITY,
                    MessageSubmission.FormatEnum.TEXT, userIdList));
        } else {
            userIdList.add(message.getFromUserId());
            aiResponseList.addResponse(new AiResponse(HelpBotConstants.SHOW_IDENTITY,
                    MessageSubmission.FormatEnum.TEXT, userIdList));
        }

        MemberCache.writeMember(member);

        return aiResponseList;
    }
}
