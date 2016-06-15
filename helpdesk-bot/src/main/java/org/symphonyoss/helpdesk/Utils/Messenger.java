package org.symphonyoss.helpdesk.utils;

import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.Stream;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class Messenger {
    public static void sendMessage(String message, MessageSubmission.FormatEnum type, Long userID, SymphonyClient symClient) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(type);
        userMessage.setMessage(message);

        Stream stream = new Stream();
        stream.setId(userID.toString());
        try {
            symClient.getMessagesClient().sendMessage(stream, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message, MessageSubmission.FormatEnum type, String email, SymphonyClient symClient) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(type);
        userMessage.setMessage(message);

        try {
            symClient.getMessageService().sendMessage(email, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message, MessageSubmission.FormatEnum type, Message refMes, SymphonyClient symClient) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(type);
        userMessage.setMessage(message);

        Stream stream = new Stream();
        stream.setId(refMes.getStream());
        try {
            symClient.getMessagesClient().sendMessage(stream, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message, MessageSubmission.FormatEnum type, Chat chat, SymphonyClient symClient) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(type);
        userMessage.setMessage(message);

        try {
            symClient.getMessageService().sendMessage(chat, userMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
