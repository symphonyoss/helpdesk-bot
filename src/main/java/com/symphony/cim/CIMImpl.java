package com.symphony.cim;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.symphony.AgentInfo;
import com.symphony.AgentSelector;
import com.symphony.sapi.AsyncCallback;

import io.vertx.core.MultiMap;

public class CIMImpl implements AgentSelector {

	private static Logger logger = LoggerFactory.getLogger(CIMImpl.class);

	@Override
	public void assignAgent(MultiMap helpRequest, AsyncCallback<AgentInfo> asyncCallback) {
		logger.info("Requesting agent assignment for new help session: " + helpRequest);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				String[] agents = { "Matt matt.harper@symphony.com", "Aaron jignesh@symphony.com",
						"Jignesh jignesh@symphony.com", };

				String agent = (String) JOptionPane.showInputDialog(null,
						"Please select an agent for the following help request: \n" + helpRequest.toString(),
						"Agent Selector", JOptionPane.QUESTION_MESSAGE, null, agents, agents[0]);

				String[] p = agent.split(" ");
				asyncCallback.callback(new AgentInfo(p[0], p[1]));
			}
		});
	}

}
