package org.symphonyoss.webservice.listeners;

import org.symphonyoss.webservice.models.web.WebMessage;

/**
 * Created by nicktarsillo on 7/8/16.
 */
public interface WebSessionListener {
    void onNewWSMessage(WebMessage webMessage);
}
