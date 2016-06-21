package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.actions.*;
import org.symphonyoss.helpdesk.models.permissions.IsMember;
import org.symphonyoss.helpdesk.models.permissions.OffCall;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class MemberResponseListener extends AiCommandListener {
    private HelpClientListener helpClientListener;

    public MemberResponseListener(SymphonyClient client, HelpClientListener helpClientListener) {
        super(client);
        this.helpClientListener = helpClientListener;
        init();
    }

    private void init() {
        AiCommand acceptNextHelpClient = new AiCommand("Accept Next Client", 0);
        acceptNextHelpClient.addAction(new AcceptHelpAction(helpClientListener, this, getSymClient()));
        acceptNextHelpClient.addPermission(new IsMember());
        acceptNextHelpClient.addPermission(new OffCall());

        AiCommand acceptHelpClient = new AiCommand("Accept ", 1);
        acceptHelpClient.setArgument(0, "Client");
        acceptHelpClient.setPrefixRequirement(0, "@");
        acceptHelpClient.addAction(new AcceptHelpAction(helpClientListener, this, getSymClient()));
        acceptHelpClient.addPermission(new IsMember());
        acceptHelpClient.addPermission(new OffCall());

        AiCommand toggleHelp = new AiCommand("Toggle Online", 0);
        toggleHelp.addAction(new ToggleSeeHelpAction());
        toggleHelp.addPermission(new IsMember());
        toggleHelp.addPermission(new OffCall());

        AiCommand toggleIdentity = new AiCommand("Toggle Show Identity", 0);
        toggleIdentity.addAction(new ToggleIdentityAction());
        toggleIdentity.addPermission(new IsMember());
        toggleIdentity.addPermission(new OffCall());

        AiCommand addMember = new AiCommand("Add Member", 1);
        addMember.setArgument(0, "Client");
        addMember.setPrefixRequirement(0, "@");
        addMember.addAction(new AddMemberAction(helpClientListener, this, getSymClient()));
        addMember.addPermission(new IsMember());
        addMember.addPermission(new OffCall());

        AiCommand joinChat = new AiCommand("Join chat ", 1);
        joinChat.setArgument(0, "Client/Member");
        joinChat.setPrefixRequirement(0, "@");
        joinChat.addAction(new JoinChatAction(getSymClient()));
        joinChat.addPermission(new IsMember());
        joinChat.addPermission(new OffCall());

        AiCommand onlineMembers = new AiCommand("Online Members", 0);
        onlineMembers.addAction(new OnlineMembersAction());

        AiCommand queueResponse = new AiCommand("Client Queue", 0);
        queueResponse.addAction(new ClientQueueAction());
        queueResponse.addPermission(new IsMember());
        queueResponse.addPermission(new OffCall());

        getActiveCommands().add(acceptNextHelpClient);
        getActiveCommands().add(acceptHelpClient);
        getActiveCommands().add(toggleHelp);
        getActiveCommands().add(toggleIdentity);
        getActiveCommands().add(onlineMembers);
        getActiveCommands().add(queueResponse);
        getActiveCommands().add(addMember);
        getActiveCommands().add(joinChat);

        setPushCommands(true);
    }
}
