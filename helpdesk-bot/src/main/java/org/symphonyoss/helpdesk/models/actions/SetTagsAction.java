package org.symphonyoss.helpdesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.LinkedHashSet;

/**
 * Created by nicktarsillo on 6/24/16.
 */
public class SetTagsAction implements AiAction {
    /**
     * Sets a members tags, as specified by the member.
     * Get the member.
     * Split the string into a list of tags.
     * Delete old tags.
     * Set new tags.
     * @param mlMessageParser  the parser containing the message in ML
     * @param message   the message received
     * @param command   the command that called this action
     * @return   success message
     */
    public AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        userIdList.add(message.getFromUserId());

        Member member = MemberCache.getMember(message);

        String text = mlMessageParser.getText();
        String[] chunks = text.substring(command.getCommand().length()).replace(",", "").trim().split("\\s+");

        member.setTags(new LinkedHashSet<String>());
        for(String tag: chunks){
            member.getTags().add(tag);
        }

        MemberCache.writeMember(member);

        aiResponseSequence.addResponse(new AiResponse("Tags have been successfully set.",
                MessageSubmission.FormatEnum.TEXT, userIdList));

        return aiResponseSequence;
    }
}
