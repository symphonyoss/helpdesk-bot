package org.symphonyoss.helpdesk.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.models.users.HelpClient;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.Messenger;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;

/**
 * Created by nicktarsillo on 6/21/16.
 * A model that allows a call to relay a message to both parties (Member and Client).
 */
public class CallResponder {
    private final Logger logger = LoggerFactory.getLogger(Call.class);
    private Call call;
    private SymphonyClient symClient;

    public CallResponder(Call call, SymphonyClient symClient) {
        this.symClient = symClient;
        this.call = call;
    }

    /**
     * Sends the room info back to the message from id.
     * @param message   the received message
     */
    public void sendRoomInfo(Message message) {
        if(message == null
                || message.getFromUserId() == null){

            if(logger != null)
                logger.error("Cannot send null message {}.", message);

            return;
        }

        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.BREAK
                + HelpBotConstants.CLIENTS_LABEL + getClientList()
                + HelpBotConstants.MEMBERS_LABEL + getMemberList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, message, symClient);

    }

    /**
     * Sends the call help summary back to the user id.
     * @param userID   the user id
     */
    public void sendHelpSummary(Long userID) {
        if(userID == null){

            if(logger != null)
                logger.error("Cannot send null userId {}.", userID);

            return;
        }

        Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.BREAK + MLTypes.BREAK + MLTypes.START_BOLD
                + HelpBotConstants.HELP_SUMMARY_LABEL + MLTypes.END_BOLD + MLTypes.BREAK + getHelpList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, userID, symClient);

    }

    /**
     * Notify a user that a client has joined the room.
     * If client email does not exist, use user id.
     *
     * @param user   the desk user to send to
     * @param client   the client who entered the room
     */
    public void sendEnteredChatMessage(DeskUser user, HelpClient client) {

        if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase("")) {

            Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.HELP_CLIENT_LABEL + MLTypes.START_BOLD +
                    client.getEmail() + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT
                    , MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

        }else {

            Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.HELP_CLIENT_LABEL + MLTypes.START_BOLD +
                    client.getUserID() + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT,
                    MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

        }

    }

    /**
     * Notify a user that a member has joined the room.
     * Retain member identity preference
     *
     * @param user   the desk user to send to
     * @param member   the member who entered the room
     */
    public void sendEnteredChatMessage(DeskUser user, Member member) {

        if (!member.isHideIdentity()) {

            Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.MEMBER_LABEL + MLTypes.START_BOLD +
                            member.getEmail() + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT + MLTypes.END_ML,
                    MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

        }else {

            Messenger.sendMessage(MLTypes.START_ML.toString() + MLTypes.START_BOLD +
                    HelpBotConstants.MEMBER_LABEL + (call.getMembers().indexOf(member) + 1) + MLTypes.END_BOLD + HelpBotConstants.ENTERED_CHAT
                    + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

        }

    }

    /**
     * Notify a user that they have successfully connected to the room
     *
     * @param user   the desk user to send to
     */
    public void sendConnectedMessage(DeskUser user) {
        if(user == null){

            if(logger != null)
                logger.error("Cannot send null user connected message {}.", user);

            return;
        }

        Messenger.sendMessage(MLTypes.START_ML + HelpBotConstants.CONNECTED_TO_CALL
                + HelpBotConstants.CLIENTS_LABEL + getClientList()
                + HelpBotConstants.MEMBERS_LABEL + getMemberList()
                + MLTypes.END_ML, MessageSubmission.FormatEnum.MESSAGEML, user.getUserID(), symClient);

    }

    /**
     * Notify the user that a client has left the room
     * If client email does not exits, use id
     *
     * @param user   the desk user to send to
     * @param client   the client that exited the room
     */
    public void sendExitMessage(DeskUser user, HelpClient client) {

        if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase("")) {

            Messenger.sendMessage(client.getEmail() + HelpBotConstants.LEFT_CALL,
                    MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);

        }else {

            Messenger.sendMessage(HelpBotConstants.HELP_CLIENT_LABEL + client.getUserID().toString()
                    + HelpBotConstants.LEFT_CALL, MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);

        }

    }

    /**
     * Notify the user that a member has left the room
     * Retain member identity preference
     *
     * @param user   the desk user to send to
     * @param member   the member that exited the room
     */
    public void sendExitMessage(DeskUser user, Member member) {

        if (!member.isHideIdentity()) {

            Messenger.sendMessage(member.getEmail() + HelpBotConstants.LEFT_CALL,
                    MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);

        }else {

            Messenger.sendMessage(HelpBotConstants.MEMBER_LABEL + (call.getMembers().indexOf(member) + 1)
                    + HelpBotConstants.LEFT_CALL, MessageSubmission.FormatEnum.TEXT, user.getUserID(), symClient);

        }

    }

    private String getClientList() {
        String list = "";

        for (HelpClient client : call.getClients()) {

            if (client.getEmail() != null && !client.getEmail().equalsIgnoreCase("")) {
                list += "," + client.getEmail();
            }else {
                list += "," + client.getUserID();
            }

        }

        return list.substring(1);
    }

    private String getHelpList() {
        String list = "";

        for (HelpClient client : call.getClients()) {
            list += client.getHelpSummary();
        }

        return list;
    }

    private String getMemberList() {
        String list = "";

        for (Member member : call.getMembers()) {

            if (!member.isHideIdentity()) {
                list += ", " + member.getEmail();
            }else {
                list += ", " + HelpBotConstants.MEMBER_LABEL + (call.getMembers().indexOf(member) + 1);
            }

        }

        return list.substring(1);
    }
}
