package com.symphony.web;

import com.symphony.session.HelpSession;

public interface HelpSessionListener {

	public void onHelpSessionInit(HelpSession helpSession);

	public void onHelpSessionTerminate(HelpSession helpSession);

}
