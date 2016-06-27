package org.symphonyoss.helpdesk.utils;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.helpdesk.config.HelpBotConfig;
import org.symphonyoss.helpdesk.models.users.Member;
import org.symphonyoss.helpdesk.models.users.SerializableMember;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.pod.model.User;

import java.io.*;
import java.util.*;
import static org.symphonyoss.helpdesk.config.HelpBotConfig.Config;
/**
 * Created by nicktarsillo on 6/14/16.
 */
public class MemberCache {
    public static final Map<String, Member> MEMBERS = new HashMap<String, Member>();
    private static final Logger logger = LoggerFactory.getLogger(MemberCache.class);

    public static void loadMembers() {

        File[] files = new File(System.getProperty("files.json")).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.equals(".DS_Store");
            }
        });

        Gson gson = new Gson();

        if (files != null) {


            for (File file : files) {

                try {

                    logger.debug(file.getName());
                    Member member = gson.fromJson(new FileReader(file), SerializableMember.class).toMember();
                    addMember(member);
                    logger.debug("Loaded member {}", member.getUserID());

                } catch (IOException e) {
                    logger.error("Could not load json {} ", file.getName(), e);
                }

            }


        }
    }

    public static void writeMember(Member member) {

        if(member == null)
            return;

        try {

            Gson gson = new Gson();
            FileWriter jsonFile = new FileWriter(System.getProperty("files.json") + member.getUserID() + ".json");
            String toJson = gson.toJson(member.toSerializable(), SerializableMember.class);

            jsonFile.write(toJson);

            jsonFile.flush();
            jsonFile.close();

        } catch (IOException e) {
            logger.error("Could not write file for hashtag {}", member.getEmail(), e);
        }

    }

    public static Member[] getBestMembers(final String helpRequest){
        ArrayList<Member> orderMembers = new ArrayList<Member>(MEMBERS.values());

        Collections.sort(orderMembers, new Comparator<Member>() {

            public int compare(Member member1, Member member2) {
                if(member1.countTagMatches(helpRequest) > member2.countTagMatches(helpRequest)){
                    return -1;
                }else if(member1.countTagMatches(helpRequest) < member2.countTagMatches(helpRequest)){
                    return 1;
                }else {
                    return 0;
                }
            }

        });

        int sendTo = 0;
        if(orderMembers.get(0).countTagMatches(helpRequest) == 0) {

            sendTo = (int) (orderMembers.size()
                    * Double.parseDouble(Config.getString(HelpBotConfig.BEST_PERCENTAGE))) + 1;

        }else {
            sendTo = orderMembers.size();
        }

        Member[] best = new Member[sendTo];
        for(int index = 0; index < sendTo; index++) {
            best[index] = orderMembers.get(index);
        }

        return best;
    }

    public static void removeMember(Member member) {
        new File(System.getProperty("files.json") + member.getUserID() + ".json").delete();

        DeskUserCache.removeUser(member);
    }

    public static void addMember(Member member) {
        MemberCache.writeMember(member);

        MemberCache.MEMBERS.put(member.getUserID().toString(), member);
        DeskUserCache.addUser(member);
    }

    public static String listMembers() {
        String list = "";
        int index = 1;
        for (Member member : MEMBERS.values()) {
            if (!member.isHideIdentity())
                list += ", " + member.getEmail();
            else
                list += ", Member " + index;
            index++;
        }

        if (MEMBERS.size() > 0)
            return list.substring(1);
        else
            return list;
    }

    public static String listOnlineMembers() {
        String list = "";
        int index = 1;
        for (Member member : MEMBERS.values()) {

            if (member.isOnline() && !member.isBusy()) {

                if (!member.isHideIdentity()) {
                    list += ", " + member.getEmail();
                }else {
                    list += ", Member " + index;
                }

            }

            index++;
        }

        if (list.length() > 0) {
            return list.substring(1);
        }else {
            return list;
        }
    }

    public static Member getMember(User user) {
        return MEMBERS.get(user.getId().toString());
    }

    public static Member getMember(Message message) {
        return MEMBERS.get(message.getFromUserId().toString());
    }

    public static Member getMember(String userID) {
        return MEMBERS.get(userID);
    }

    public static boolean hasMember(String userID) {
        return MEMBERS.containsKey(userID);
    }
}