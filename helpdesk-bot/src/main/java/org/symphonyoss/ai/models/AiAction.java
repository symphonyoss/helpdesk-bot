package org.symphonyoss.ai.models;

import org.symphonyoss.client.util.MlMessageParser;
import org.symphonyoss.symphony.agent.model.Message;

/**
 * Created by nicktarsillo on 6/20/16.
 */
public interface AiAction {
    AiResponseList respond(MlMessageParser mlMessageParser, Message message, AiCommand command);
}
