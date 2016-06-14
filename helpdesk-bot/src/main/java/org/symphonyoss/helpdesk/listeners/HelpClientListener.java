package org.symphonyoss.helpdesk.listeners;

import Constants.HelpBotConstants;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.helpdesk.models.MemberDatabase;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.pod.model.Stream;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpClientListener implements ChatListener {
    private SymphonyClient symClient;

    public HelpClientListener(SymphonyClient symClient){
        this.symClient = symClient;
    }

    public void onChatMessage(Message message) {
        HelpBotConstants.ALLCLIENTS.get(message.getId()).getHelpRequests().add(message.toString());

        for(Member member : MemberDatabase.members.values())
            if(!member.isOnCall() && member.isSeeCommands())
                if(HelpBotConstants.ALLCLIENTS.get(message.getId()).getEmail() != null &&
                        HelpBotConstants.ALLCLIENTS.get(message.getId()).getEmail() != "") {
                    notifyUser(HelpBotConstants.ALLCLIENTS.get(message.getId()).getEmail() + ": "
                            + message.toString(), member.getUserID());
                }else{
                    notifyUser(message.getId() + ": " + message.getMessage(), member.getUserID());
                }
    }

    void notifyUser(String message, long userID) {
        MessageSubmission userMessage = new MessageSubmission();
        userMessage.setFormat(MessageSubmission.FormatEnum.TEXT);
        userMessage.setMessage(message);
        Stream stream = new Stream();
        stream.setId(""+userID);
        try {
            symClient.getMessagesClient().sendMessage(stream, userMessage);
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
