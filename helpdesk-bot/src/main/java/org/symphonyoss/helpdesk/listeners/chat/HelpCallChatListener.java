package org.symphonyoss.helpdesk.listeners.chat;

import org.symphonyoss.ai.constants.MLTypes;
import org.symphonyoss.ai.listeners.AiCommandListener;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.constants.HelpBotConstants;
import org.symphonyoss.helpdesk.listeners.command.CallCommandListener;
import org.symphonyoss.helpdesk.models.calls.Call;
import org.symphonyoss.helpdesk.models.calls.HelpCall;
import org.symphonyoss.helpdesk.models.users.DeskUser;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.utils.MemberCache;

/**
 * Created by nicktarsillo on 6/27/16.
 */
public class HelpCallChatListener extends CallChatListener{
    private  HelpCall helpCall;
    public HelpCallChatListener(HelpCall call, CallCommandListener callCommandListener,SymphonyClient symClient) {
        super(call,  callCommandListener , symClient);
        helpCall = call;
    }

    @Override
    protected String constructRelayMessage(DeskUser deskUser, String text){
        if(deskUser.getUserType() == DeskUser.DeskUserType.MEMBER){

            Member member = MemberCache.getMember(deskUser.getUserID().toString());
            if(member.isHideIdentity()){

                return MLTypes.START_BOLD.toString() + HelpBotConstants.MEMBER_LABEL
                        + (helpCall.getMembers().indexOf(member) + 1) + ": " + MLTypes.END_BOLD + text;

            }else{

                return MLTypes.START_BOLD.toString()
                        + deskUser.getEmail() + ": " + MLTypes.END_BOLD + text;

            }

        }else{
            return super.constructRelayMessage(deskUser, text);
        }
    }
}
