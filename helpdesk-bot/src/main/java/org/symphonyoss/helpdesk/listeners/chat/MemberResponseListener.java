package org.symphonyoss.helpdesk.listeners.chat;

import org.symphonyoss.botresponse.listeners.BotResponseListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.models.responses.*;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class MemberResponseListener extends BotResponseListener{
    private HelpClientListener helpClientListener;

    public MemberResponseListener(SymphonyClient client, HelpClientListener helpClientListener){
        super(client);
        this.helpClientListener = helpClientListener;
        init();
    }

    private void init(){
        AcceptHelpResponse acceptNextHelpClient = new AcceptHelpResponse("Accept Next Client", 0, helpClientListener);

        AcceptHelpResponse acceptHelpClient = new AcceptHelpResponse("Accept ", 1, helpClientListener);
        acceptHelpClient.setPlaceHolder(0, "Client");
        acceptHelpClient.setPrefixRequirement(0, "@");

        ToggleSeeHelpResponse toggleHelp = new ToggleSeeHelpResponse("Toggle Online", 0);

        ToggleIdentityResponse toggleIdentity = new ToggleIdentityResponse("Toggle Show Identity", 0);

        AddMemberResponse addMember = new AddMemberResponse("Add Member", 1, helpClientListener);
        addMember.setPlaceHolder(0, "Client");
        addMember.setPrefixRequirement(0, "@");

        JoinChatResponse joinChat = new JoinChatResponse("Join chat ", 1);
        joinChat.setPlaceHolder(0, "Client/Member");
        joinChat.setPrefixRequirement(0, "@");

        OnlineMembersResponse onlineMembers = new OnlineMembersResponse("Online Members", 0);

        ClientQueueResponse queueResponse  = new ClientQueueResponse("Client Queue", 0);

        getActiveResponses().add(acceptNextHelpClient);
        getActiveResponses().add(acceptHelpClient);
        getActiveResponses().add(toggleHelp);
        getActiveResponses().add(toggleIdentity);
        getActiveResponses().add(onlineMembers);
        getActiveResponses().add(queueResponse);
        getActiveResponses().add(addMember);
        getActiveResponses().add(joinChat);

        setPushCommands(true);
    }
}
