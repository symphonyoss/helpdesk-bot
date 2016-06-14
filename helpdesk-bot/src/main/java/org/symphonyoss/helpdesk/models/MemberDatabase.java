package org.symphonyoss.helpdesk.models;

import Constants.HelpBotConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.helpdesk.models.users.Member;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by nicktarsillo on 6/14/16.
 */
public class MemberDatabase {
    private static Logger logger = LoggerFactory.getLogger(MemberDatabase.class);
    public static Map<String, Member> members = new HashMap<String, Member>();

    private void writeMember(Member member) {
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


    public static void removeMember(Member member){
        new File(System.getProperty("files.json") + member.getUserID() + ".json").delete();
    }

    public static void loadMembers(){
        File[] files = new File(System.getProperty("files.json")).listFiles();

        Gson gson = new Gson();

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
