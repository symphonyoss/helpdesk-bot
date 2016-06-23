package org.symphonyoss.helpdesk.models;

import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.listeners.chat.HelpClientListener;
import org.symphonyoss.helpdesk.listeners.command.MemberCommandListener;

/**
 * Created by nicktarsillo on 6/23/16.
 */
public class HelpBotSession {
    private SymphonyClient symphonyClient;
    private HelpClientListener helpClientListener;
    private MemberCommandListener memberListener;

    public HelpBotSession(){}

    public HelpBotSession(SymphonyClient symphonyClient, HelpClientListener helpClientListener, MemberCommandListener memberCommandListener){
        this.symphonyClient = symphonyClient;
        this.helpClientListener = helpClientListener;
        this.memberListener = memberCommandListener;
    }

    public SymphonyClient getSymphonyClient() {
        return symphonyClient;
    }

    public void setSymphonyClient(SymphonyClient symphonyClient) {
        this.symphonyClient = symphonyClient;
    }

    public HelpClientListener getHelpClientListener() {
        return helpClientListener;
    }

    public void setHelpClientListener(HelpClientListener helpClientListener) {
        this.helpClientListener = helpClientListener;
    }

    public MemberCommandListener getMemberListener() {
        return memberListener;
    }

    public void setMemberListener(MemberCommandListener memberListener) {
        this.memberListener = memberListener;
    }
}
