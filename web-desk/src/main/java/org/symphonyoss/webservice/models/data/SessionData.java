package org.symphonyoss.webservice.models.data;

/**
 * Represents the data a user provides before entering a sessions.
 */
public class SessionData {
    private String name;
    private String email;
    private String topic;

    public SessionData(String name, String email, String topic) {
        this.name = name;
        this.email = email;
        this.topic = topic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
