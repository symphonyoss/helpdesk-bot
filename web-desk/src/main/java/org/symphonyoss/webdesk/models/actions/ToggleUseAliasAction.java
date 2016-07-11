package org.symphonyoss.webdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;
import org.symphonyoss.webdesk.models.users.Member;
import org.symphonyoss.webdesk.utils.MemberCache;

/**
 * Created by nicktarsillo on 7/7/16.
 */
public class ToggleUseAliasAction implements AiAction {
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        userIdList.add(message.getFromUserId());

        Member member = MemberCache.getMember(message);

        member.setUseAlias(!member.isUseAlias());

        MemberCache.writeMember(member);

        if (member.isUseAlias()) {

            aiResponseSequence.addResponse(new AiResponse("Alias use enabled.",
                    MessageSubmission.FormatEnum.TEXT, userIdList));


        } else {

            aiResponseSequence.addResponse(new AiResponse("Alias use disabled.",
                    MessageSubmission.FormatEnum.TEXT, userIdList));

        }

        return aiResponseSequence;
    }
}
