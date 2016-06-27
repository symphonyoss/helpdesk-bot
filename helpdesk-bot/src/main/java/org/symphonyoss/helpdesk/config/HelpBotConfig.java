package org.symphonyoss.helpdesk.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class HelpBotConfig {

    public static final Configuration Config;
    private final static Logger logger = LoggerFactory.getLogger(HelpBotConfig.class);


    //Properties
    public final static String CONFIG_DIR = "helpbot.config.dir";
    public final static String CONFIG_FILE = "helpbot.properties";
    public final static String ACCEPT_NEXT = "helpbot.command.acceptnext";
    public final static String ACCEPT = "helpbot.command.accept";
    public final static String TOGGLE_ONLINE = "helpbot.command.toggleonline";
    public final static String TOGGLE_SHOW_IDENTITY = "helpbot.command.toggleidentity";
    public final static String ADD_MEMBER = "helpbot.command.addmember";
    public final static String JOIN_CHAT = "helpbot.command.joinchat";
    public final static String ONLINE_MEMBERS = "helpbot.command.onlinemembers";
    public final static String CLIENT_QUEUE = "helpbot.command.clientqueue";
    public final static String MY_SETTINGS = "helpbot.command.mysettings";
    public final static String HELP_SUMMARY = "helpbot.command.helpsummary";
    public final static String ROOM_INFO = "helpbot.command.roominfo";
    public final static String EXIT = "helpbot.command.exit";
    public final static String SET_TAGS = "helpbot.command.settags";
    public final static String ADD_TAGS = "helpbot.command.addtags";

    public static final String BEST_PERCENTAGE = "helpbot.setting.bestpercentage";

    public final static String REMOVE_TAGS = "helpbot.command.removetags";
    public final static String KEYSTORE_PASSWORD = "keystore.password";
    public final static String TRUSTSTORE_PASSWORD = "truststore.password";
    public final static String SESSIONAUTH_URL = "sessionauth.url";
    public final static String KEYAUTH_URL = "keyauth.url";
    public final static String SYMPHONY_POD = "symphony.agent.pod.url";
    public final static String SYMPHONY_AGENT = "symphony.agent.agent.url";
    public final static String CERTS_DIR = "certs.dir";
    public final static String TRUSTSTORE_FILE = "truststore.file";
    public final static String BOT_USER = "bot.user";
    public final static String FILES_JSON = "files.json";
    public final static String ADMIN_USER = "admin.user";


    //Env
    public final static String KEYSTORE_PASSWORD_ENV = "KEYSTORE_PASSWORD";
    public final static String TRUSTSTORE_PASSWORD_ENV = "TRUSTSTORE_PASSWORD";
    public final static String SESSIONAUTH_URL_ENV = "SESSION_AUTH";
    public final static String KEYAUTH_URL_ENV = "KEY_AUTH";
    public final static String SYMPHONY_POD_ENV = "SYMPHONY_POD";
    public final static String SYMPHONY_AGENT_ENV = "SYMPHONY_AGENT";
    public final static String CERTS_DIR_ENV = "CERTS";
    public final static String TRUSTSTORE_FILE_ENV = "TRUSTSTORE_FILE";
    public final static String BOT_USER_ENV = "BOT_USER";
    public final static String FILES_JSON_ENV = "FILES_JSON";
    public final static String ADMIN_USER_ENV = "ADMIN_USER";


    static {


        String configDir = null;
        String propFile = null;


        Configuration c = null;


        try {

            if (configDir == null) {
                configDir = System.getProperty(CONFIG_DIR);
                if (configDir == null)
                    configDir = "config";
            }

            if (propFile == null)
                propFile = CONFIG_FILE;

            propFile = configDir + "/" + propFile;

            logger.info("Using helpbot.properties file location: {}", propFile);

            c = new PropertiesConfiguration(propFile);


        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            logger.error("Configuration Init Exception: ", e);
            c = null;
        }

        Config = c;

        init();


    }


    public static Configuration getConfig() {
        return Config;
    }

    private static void init() {

        //The following defines the order of variables and configuration from Env variables, to System properties,
        // to Config driven.
        // System properties->Env Variables->Conig driven
        // Note: Both System and/or Env will be pushed into local "Config" properties.


        if (System.getProperty(KEYSTORE_PASSWORD) == null) {

            if (System.getenv(KEYSTORE_PASSWORD_ENV) != null) {
                System.setProperty(KEYSTORE_PASSWORD, System.getenv(KEYSTORE_PASSWORD_ENV));
            } else {
                System.setProperty(KEYSTORE_PASSWORD, Config.getString(KEYSTORE_PASSWORD));
            }

        }

        if (System.getProperty(TRUSTSTORE_PASSWORD) == null) {

            if (System.getenv(TRUSTSTORE_PASSWORD_ENV) != null) {
                System.setProperty(TRUSTSTORE_PASSWORD, System.getenv(TRUSTSTORE_PASSWORD_ENV));
            } else {
                System.setProperty(TRUSTSTORE_PASSWORD, Config.getString(TRUSTSTORE_PASSWORD));
            }

        }

        if (System.getProperty(SESSIONAUTH_URL) == null) {

            if (System.getenv(SESSIONAUTH_URL_ENV) != null) {
                System.setProperty(SESSIONAUTH_URL, System.getenv(SESSIONAUTH_URL_ENV));
            } else {
                System.setProperty(SESSIONAUTH_URL, Config.getString(SESSIONAUTH_URL));
            }

        }

        if (System.getProperty(KEYAUTH_URL) == null) {

            if (System.getenv(KEYAUTH_URL_ENV) != null) {
                System.setProperty(KEYAUTH_URL, System.getenv(KEYAUTH_URL_ENV));
            } else {
                System.setProperty(KEYAUTH_URL, Config.getString(KEYAUTH_URL));
            }

        }

        if (System.getProperty(SYMPHONY_POD) == null) {

            if (System.getenv(SYMPHONY_POD_ENV) != null) {
                System.setProperty(SYMPHONY_POD, System.getenv(SYMPHONY_POD_ENV));
            } else {
                System.setProperty(SYMPHONY_POD, Config.getString(SYMPHONY_POD));
            }

        }

        if (System.getProperty(SYMPHONY_AGENT) == null) {

            if (System.getenv(SYMPHONY_AGENT_ENV) != null) {
                System.setProperty(SYMPHONY_AGENT, System.getenv(SYMPHONY_AGENT_ENV));
            } else {
                System.setProperty(SYMPHONY_AGENT, Config.getString(SYMPHONY_AGENT));
            }

        }

        if (System.getProperty(CERTS_DIR) == null) {

            if (System.getenv(CERTS_DIR_ENV) != null) {
                System.setProperty(CERTS_DIR, System.getenv(CERTS_DIR_ENV));
            } else {
                System.setProperty(CERTS_DIR, Config.getString(CERTS_DIR));
            }

        }

        if (System.getProperty(TRUSTSTORE_FILE) == null) {

            if (System.getenv(TRUSTSTORE_FILE_ENV) != null) {
                System.setProperty(TRUSTSTORE_FILE, System.getenv(TRUSTSTORE_FILE_ENV));
            } else {
                System.setProperty(TRUSTSTORE_FILE, Config.getString(TRUSTSTORE_FILE));
            }

        }

        if (System.getProperty(BOT_USER) == null) {

            if (System.getenv(BOT_USER_ENV) != null) {
                System.setProperty(BOT_USER, System.getenv(BOT_USER_ENV));
            } else {
                System.setProperty(BOT_USER, Config.getString(BOT_USER));
            }

        }

        if (System.getProperty(FILES_JSON) == null) {

            if (System.getenv(FILES_JSON_ENV) != null) {
                System.setProperty(FILES_JSON, System.getenv(FILES_JSON_ENV));
            } else {
                System.setProperty(FILES_JSON, Config.getString(FILES_JSON));
            }

        }

        if (System.getProperty(ADMIN_USER) == null) {

            if (System.getenv(ADMIN_USER_ENV) != null) {
                System.setProperty(ADMIN_USER, System.getenv(ADMIN_USER_ENV));
            } else {
                System.setProperty(ADMIN_USER, Config.getString(ADMIN_USER));
            }

        }

    }

}
