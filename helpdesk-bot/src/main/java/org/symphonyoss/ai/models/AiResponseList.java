package org.symphonyoss.ai.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public class AiResponseList {
    private Set<AiResponse> aiResponseSet = new HashSet<AiResponse>();

    public AiResponseList() {

    }

    public void addResponse(AiResponse response) {
        aiResponseSet.add(response);
    }

    public void removeResponse(AiResponse response) {
        aiResponseSet.remove(response);
    }

    public Set<AiResponse> getAiResponseSet() {
        return aiResponseSet;
    }

    public void setAiResponseSet(Set<AiResponse> aiResponseSet) {
        this.aiResponseSet = aiResponseSet;
    }
}
