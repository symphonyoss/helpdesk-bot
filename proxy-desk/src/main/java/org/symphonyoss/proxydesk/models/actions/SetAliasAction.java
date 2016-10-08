package org.symphonyoss.proxydesk.models.actions;

import org.symphonyoss.ai.models.AiAction;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.ai.models.AiResponse;
import org.symphonyoss.ai.models.AiResponseSequence;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.proxydesk.models.users.Member;
import org.symphonyoss.proxydesk.utils.MemberCache;
import org.symphonyoss.symphony.clients.model.SymMessage;

import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.LinkedHashSet;

/**
 * Created by nicktarsillo on 7/14/16.
 */
public class SetAliasAction implements AiAction{
    public AiResponseSequence respond(MlMessageParser mlMessageParser, SymMessage message, AiCommand command) {
        AiResponseSequence aiResponseSequence = new AiResponseSequence();
        UserIdList userIdList = new UserIdList();
        userIdList.add(message.getFromUserId());

        Member member = MemberCache.getMember(message);

        String text = mlMessageParser.getText();
        text = text.substring(command.getCommand().length()).trim();

        member.setAlias(text);

        MemberCache.writeMember(member);

        aiResponseSequence.addResponse(new AiResponse("Alias have been successfully set.",
                SymMessage.Format.TEXT, userIdList));

        return aiResponseSequence;
    }
}
