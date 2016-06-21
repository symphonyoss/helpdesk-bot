package org.symphonyoss.helpdesk.listeners.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.command.CallResponseListener;
import org.symphonyoss.helpdesk.models.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.ClientCache;
import org.symphonyoss.helpdesk.utils.MemberCache;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

import java.util.HashMap;

/**
 * Created by nicktarsillo on 6/21/16.
 */
public class CallChatListener implements ChatListener {
    private final Logger logger = LoggerFactory.getLogger(CallChatListener.class);
    private HashMap<String, Boolean> entered = new HashMap<String, Boolean>();
    private Call call;
    private AiCommandListener callResponseListener;
    private SymphonyClient symClient;

    public CallChatListener(Call call, AiCommandListener callResponseListener, SymphonyClient symClient){
        this.callResponseListener = callResponseListener;
        this.symClient = symClient;
        this.call = call;
    }

    public void onChatMessage(Message message) {
        if (callResponseListener.isCommand(message)
                || isPushMessage(message))
            return;


        MlMessageParser mlMessageParser;

        try {
            mlMessageParser = new MlMessageParser(symClient);
            mlMessageParser.parseMessage(message.getMessage());
        } catch (Exception e) {
            logger.error("Could not parse message {}", message.getMessage(), e);
            return;
        }

        String text = mlMessageParser.getText();
        Member member = null;
        if (MemberCache.hasMember(message.getFromUserId().toString()))
            member = MemberCache.getMember(message);

        if (member != null)
            relayMemberMessage(member, text);
        else
            relayClientMessage(ClientCache.retrieveClient(message), text);
        call.setInactivityTime(0);
    }

    public void listenOn(Chat chat){
        chat.registerListener(this);
        entered.put(chat.getStream().getId(), true);
    }

    public void stopListening(Chat chat){
        chat.registerListener(this);
        entered.put(chat.getStream().getId().toString(), false);
    }

    private boolean isPushMessage(Message message) {
        return (entered.get(message.getStream()) == null
                || !entered.get(message.getStream()));
    }

    private void relayMemberMessage(Member member, String text) {
        for (Member m : call.getMembers()) {
            if (member != m) {
                if (!member.isHideIdentity())
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + member.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + HelpBotConstants.MEMBER_LABEL + call.getMembers().indexOf(member) + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
            }
        }
        for (HelpClient client : call.getClients()) {
            if (!member.isHideIdentity())
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + member.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getEmail(), symClient);
            else
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + HelpBotConstants.MEMBER_LABEL + call.getMembers().indexOf(member) + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, client.getEmail(), symClient);
        }
    }

    private void relayClientMessage(HelpClient client, String text) {
        for (Member m : call.getMembers()) {
            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
            else
                Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                        MessageSubmission.FormatEnum.MESSAGEML, m.getEmail(), symClient);
        }
        for (HelpClient c : call.getClients()) {
            if (c != client) {
                if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase(""))
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getEmail() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, c.getEmail(), symClient);
                else
                    Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD
                                    + client.getUserID() + ": " + MLTypes.END_BOLD + text + MLTypes.END_ML,
                            MessageSubmission.FormatEnum.MESSAGEML, c.getEmail(), symClient);
            }
        }
    }
}
