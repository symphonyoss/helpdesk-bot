package org.symphonyoss.webdesk.listeners.chat;

import org.slf4j.Logger;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.webdesk.config.WebBotConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Listens on web, and writes messages into a file.
 */
public class TranscriptListener implements ChatListener {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(TranscriptListener.class);
    private SymphonyClient symClient;

    public TranscriptListener(SymphonyClient symClient) {
        this.symClient = symClient;
    }

    public void onChatMessage(Message message) {
        if(message == null
                || message.getFromUserId() == null)
            return;

        MlMessageParser mlMessageParser = null;
        try {

            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());

        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }

        try {
            write(message.getFromUserId() + ", "
                    + symClient.getUsersClient().getUserFromId(message.getFromUserId()).getEmailAddress()
                    + ": " + mlMessageParser.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(String content) {
        try {

            File file = new File(System.getProperty(WebBotConfig.FILES_TRANSCRIPT) + "transcript.txt");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            bw.write(System.lineSeparator() + dateFormat.format(date) + ": " + content);
            bw.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
