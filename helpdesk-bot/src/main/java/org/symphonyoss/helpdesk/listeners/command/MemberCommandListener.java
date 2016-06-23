package org.symphonyoss.helpdesk.listeners.command;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.ai.models.AiCommand;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.config.HelpBotConfig;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.models.HelpBotSession;
import org.symphonyoss.helpdesk.models.actions.*;
import org.symphonyoss.helpdesk.models.permissions.IsMember;
import org.symphonyoss.helpdesk.models.permissions.OffCall;
import static org.symphonyoss.helpdesk.config.HelpBotConfig.Config;

import javax.security.auth.login.Configuration;

/**
 * Created by nicktarsillo on 6/20/16.
 * A extension of the ai command listener.
 * Initializes all commands a member can command the ai.
 */
public class MemberCommandListener extends AiCommandListener {
    private HelpBotSession helpBotSession;

    public MemberCommandListener(HelpBotSession helpBotSession) {
        super(helpBotSession.getSymphonyClient());
        this.helpBotSession = helpBotSession;
        init();
    }

    public void init() {

        AiCommand acceptNextHelpClient = new AiCommand(Config.getString(HelpBotConfig.ACCEPT_NEXT), 0);
        acceptNextHelpClient.addAction(new AcceptHelpAction(helpBotSession));
        acceptNextHelpClient.addPermission(new IsMember());
        acceptNextHelpClient.addPermission(new OffCall());

        AiCommand acceptHelpClient = new AiCommand(Config.getString(HelpBotConfig.ACCEPT), 1);
        acceptHelpClient.setArgument(0, "Client");
        acceptHelpClient.setPrefixRequirement(0, "@");
        acceptHelpClient.addAction(new AcceptHelpAction(helpBotSession));
        acceptHelpClient.addPermission(new IsMember());
        acceptHelpClient.addPermission(new OffCall());

        AiCommand toggleHelp = new AiCommand(Config.getString(HelpBotConfig.TOGGLE_ONLINE), 0);
        toggleHelp.addAction(new ToggleSeeHelpAction());
        toggleHelp.addPermission(new IsMember());
        toggleHelp.addPermission(new OffCall());

        AiCommand toggleIdentity = new AiCommand(Config.getString(HelpBotConfig.TOGGLE_SHOW_IDENTITY), 0);
        toggleIdentity.addAction(new ToggleIdentityAction());
        toggleIdentity.addPermission(new IsMember());
        toggleIdentity.addPermission(new OffCall());

        AiCommand addMember = new AiCommand(Config.getString(HelpBotConfig.ADD_MEMBER), 1);
        addMember.setArgument(0, "Client");
        addMember.setPrefixRequirement(0, "@");
        addMember.addAction(new AddMemberAction(helpBotSession));
        addMember.addPermission(new IsMember());
        addMember.addPermission(new OffCall());

        AiCommand joinChat = new AiCommand(Config.getString(HelpBotConfig.JOIN_CHAT), 1);
        joinChat.setArgument(0, "Client/Member");
        joinChat.setPrefixRequirement(0, "@");
        joinChat.addAction(new JoinChatAction(symClient));
        joinChat.addPermission(new IsMember());
        joinChat.addPermission(new OffCall());

        AiCommand onlineMembers = new AiCommand(Config.getString(HelpBotConfig.ONLINE_MEMBERS), 0);
        onlineMembers.addAction(new OnlineMembersAction());

        AiCommand queueResponse = new AiCommand(Config.getString(HelpBotConfig.CLIENT_QUEUE), 0);
        queueResponse.addAction(new ClientQueueAction());
        queueResponse.addPermission(new IsMember());
        queueResponse.addPermission(new OffCall());

        AiCommand mySettings = new AiCommand(Config.getString(HelpBotConfig.MY_SETTINGS), 0);
        mySettings.addAction(new MySettingsAction());
        mySettings.addPermission(new IsMember());
        mySettings.addPermission(new OffCall());

        getActiveCommands().add(acceptNextHelpClient);
        getActiveCommands().add(acceptHelpClient);
        getActiveCommands().add(toggleHelp);
        getActiveCommands().add(toggleIdentity);
        getActiveCommands().add(onlineMembers);
        getActiveCommands().add(queueResponse);
        getActiveCommands().add(addMember);
        getActiveCommands().add(joinChat);
        getActiveCommands().add(mySettings);

        setPushCommands(true);
    }



}
