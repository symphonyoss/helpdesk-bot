package org.symphonyoss.ai.models;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/20/16.
 * An interface that developers can implement to create their own ai actions
 */
public interface AiAction {
    AiResponseSequence respond(MlMessageParser mlMessageParser, Message message, AiCommand command);
}
