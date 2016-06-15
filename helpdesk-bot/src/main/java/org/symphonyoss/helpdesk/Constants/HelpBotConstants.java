package org.symphonyoss.helpdesk.constants;

import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpBotConstants {
    public static final ArrayList<HelpClient> ONHOLD = new ArrayList<HelpClient>();
    public static final HashSet<Call> ACTIVECALLS = new HashSet<Call>();
    public static final Map<String, HelpClient> ALLCLIENTS = new HashMap<String, HelpClient>();

    public static final String ADMINEMAIL = "nicholas.tarsillo@markit.com";

}
