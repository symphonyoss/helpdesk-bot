package Constants;

import org.symphonyoss.helpdesk.listeners.Call;
import org.symphonyoss.helpdesk.models.users.HelpClient;

import java.util.*;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class HelpBotConstants {
    public static final ArrayList<HelpClient> ONHOLD = new ArrayList<HelpClient>();
    public static final HashSet<Call> ACTIVECALLS = new HashSet<Call>();
    public static final Map<String, HelpClient> ALLCLIENTS = new HashMap<String, HelpClient>();

    public static final String DEFAULTDATABASEURL = "";
}
