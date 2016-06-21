package org.symphonyoss.ai.models;

import org.symphonyoss.ai.constants.AiConstants;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.UserIdList;

import java.util.LinkedList;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class AiResponder {
    private SymphonyClient symClient;

    public AiResponder(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public void respondWith(Set<AiResponseList> responseLists) {
        for (AiResponseList list : responseLists)
            for (AiResponse response : list.getAiResponseSet())
                for (Long userID : response.getToIDs())
                    sendMessage(response.getMessage(), response.getType(), userID, symClient);
    }

    public void sendSuggestionMessage(AiLastCommand guess, Message message) {
                sendMessage(MLTypes.START_ML + AiConstants.SUGGEST
                        + MLTypes.START_BOLD + guess.getMlMessageParser().getText()
                        + MLTypes.END_BOLD + AiConstants.USE_SUGGESTION + MLTypes.END_ML,
                MessageSubmission.FormatEnum.MESSAGEML, message.getFromUserId(), symClient);
    }

    public void sendUsage(Message message, MlMessageParser mlMessageParser, LinkedList<AiCommand> activeCommands) {
        MessageSubmission aMessage = new MessageSubmission();
        aMessage.setFormat(MessageSubmission.FormatEnum.MESSAGEML);

        String usage = MLTypes.START_ML + mlMessageParser.getText() + AiConstants.NOT_INTERPRETABLE
                + MLTypes.BREAK + MLTypes.START_BOLD
                + AiConstants.USAGE + MLTypes.END_BOLD + MLTypes.BREAK;
        for (AiCommand command : activeCommands)
            if (command.userIsPermitted(message.getFromUserId()))
                usage += command.toMLCommand();

        usage += MLTypes.END_ML;

        sendMessage(usage, MessageSubmission.FormatEnum.MESSAGEML, message.getFromUserId(), symClient);
    }

    public void sendNoPermission(Message message) {
        Messenger.sendMessage(AiConstants.NO_PERMISSION,
                MessageSubmission.FormatEnum.TEXT, message, symClient);
    }

    public static void sendMessage(String message, MessageSubmission.FormatEnum type, Long userID, SymphonyClient symClient) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(type);
        userMessage.setMessage(message);

        UserIdList list = new UserIdList();
        list.add(userID);
        try {
            symClient.getMessagesClient().sendMessage(symClient.getStreamsClient().getStream(list), userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SymphonyClient getSymClient() {
        return symClient;
    }

    public void setSymClient(SymphonyClient symClient) {
        this.symClient = symClient;
    }
}
