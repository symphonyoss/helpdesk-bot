package org.symphonyoss.helpdesk.utils;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.helpdesk.models.users.Member;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class MemberDatabase {
    public static Map<String, Member> members = new HashMap<String, Member>();
    private static Logger logger = LoggerFactory.getLogger(MemberDatabase.class);

    public static void removeMember(Member member) {
        new File(System.getProperty("files.json") + member.getUserID() + ".json").delete();
    }

    public static void loadMembers() {
        File[] files = new File(System.getProperty("files.json")).listFiles();

        Gson gson = new Gson();
        if (files != null) {
            for (File file : files) {
                try {
                    Member member = gson.fromJson(new FileReader(file), Member.class);
                    members.put(member.getUserID().toString(), member);

                } catch (IOException e) {
                    logger.error("Could not load json {} ", file.getName(), e);
                }
            }
        }
    }

    public static void writeMember(Member member) {
        try {
            Gson gson = new Gson();
            FileWriter jsonFile = new FileWriter(System.getProperty("files.json") + member.getUserID() + ".json");
            gson.toJson(member, jsonFile);
            jsonFile.flush();
            jsonFile.close();

        } catch (IOException e) {
            logger.error("Could not write file for hashtag {}", member.getEmail(), e);
        }
    }
}
