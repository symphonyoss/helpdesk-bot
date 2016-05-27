/*
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.collaboration.virtualdesk.handler.desk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.symphonyoss.collaboration.virtualdesk.data.PresenceType;
import org.symphonyoss.collaboration.virtualdesk.data.User;
import org.symphonyoss.collaboration.virtualdesk.handler.AbstractActionHandler;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.symphonyoss.collaboration.virtualdesk.config.IServiceConfiguration;
import org.symphonyoss.collaboration.virtualdesk.data.Affiliation;
import org.symphonyoss.collaboration.virtualdesk.data.Role;
import org.symphonyoss.collaboration.virtualdesk.data.UserState;
import org.symphonyoss.collaboration.virtualdesk.data.WorkflowState;
import org.symphonyoss.collaboration.virtualdesk.muc.Desk;
import org.symphonyoss.collaboration.virtualdesk.packet.MessageResponse;
import org.symphonyoss.collaboration.virtualdesk.packet.PresenceResponse;
import org.symphonyoss.collaboration.virtualdesk.persistence.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OccupantJoinedHandler extends AbstractActionHandler
{
	private static final Logger Logger = LoggerFactory.getLogger(OccupantJoinedHandler.class);
	
	private Presence presence;

	private Desk deskRoom;
	
	private IServiceConfiguration serviceConfiguration;

	public OccupantJoinedHandler(Presence presence, Desk deskRoom, IServiceConfiguration serviceConfiguration)
	{
		this.presence = presence;
		this.deskRoom = deskRoom;
		this.serviceConfiguration = serviceConfiguration;
	}

	@Override
	protected void internalHandle() {
		JID senderJID = presence.getFrom();
		String senderNickname = presence.getTo().getResource();

		MDC.put("deskjid", presence.getTo().toBareJID());

		Logger.debug("{} is joinning the desk [{}]", senderJID, deskRoom.getJID());

		String senderBareJID = senderJID.toBareJID();

		User senderUser = null;

		if (deskRoom.isNicknameExisted(senderNickname)) {
			senderUser = deskRoom.getOccupantByNickname(senderNickname);

			// This is to check the nickname that existed is from the same JID or not.
			// If it's not from the same JID, desk has to reject the join request but
			// If it's from the same JID, it could be from the previous join cannot deliver
			// to the joiner. So when he joins again, it should be able to join with the 
			// existing nickname.
			if (!senderUser.getJID().toBareJID().equalsIgnoreCase(senderBareJID)) {
				// Nickname has already existed, so return nickname conflict
				packetList.add(PresenceResponse.createPresenceError(presence, PacketError.Condition.conflict));

				return;
			}
		}

		boolean isDeskOwner = deskRoom.isOwner(senderBareJID);
		boolean isDeskAdmin = deskRoom.isAdmin(senderBareJID);
		boolean isDeskMember = deskRoom.isMember(senderBareJID);

		if (isDeskOwner || isDeskAdmin || isDeskMember) {
			if (senderUser == null) {
				if (isDeskOwner) {
					senderUser = new User(senderJID, senderNickname, Affiliation.owner, Role.moderator);
				} else if (isDeskAdmin) {
					senderUser = new User(senderJID, senderNickname, Affiliation.admin, Role.moderator);
				} else {
					senderUser = new User(senderJID, senderNickname, Affiliation.member, Role.moderator);
				}
			}
			// Send all occupants's presences to this new joiner user
			packetList.addAll(PresenceResponse.createJoinResponse(deskRoom.getCurrentMembers(), senderUser, deskRoom.getJID(), false));
			packetList.addAll(PresenceResponse.createJoinResponse(deskRoom.getCurrentParticipants(), senderUser, deskRoom.getJID(), false));

			// Update virtual desk's presence to online to all participants if this user is the first admin in the desk
			if (deskRoom.getCurrentMemberCount() == 0) {
				Logger.debug("Updating virtual desk user's presence to all participants in desk [{}] from first desk member joining",
						deskRoom.getJID());

				packetList.addAll(PresenceResponse.createPresenceUpdate(deskRoom.getVirtualDeskUser(), deskRoom.getCurrentParticipants(), deskRoom.getJID(), PresenceType.Online));
			}

			// Send recent questions to new joiner
			sendRecentQuestions(senderUser);
		} else {
			if (senderUser == null) {
				senderUser = new User(senderJID, senderNickname, Affiliation.none, Role.participant);
			}

			if (deskRoom.isMembersOnly()) {
				Set<String> participantList = deskRoom.getParticipants();
				if (!participantList.contains(senderUser.getJID().toBareJID())) {
					packetList.add(PresenceResponse.createPresenceError(senderUser,
							deskRoom.getJID(),
							PacketError.Condition.not_allowed));

					Logger.info("{} cannot join the desk [{}] because desk is members only and user is not in the member list.",
							senderJID, deskRoom.getJID());

					return;
				}
			}

			packetList.add(PresenceResponse.createJoinResponse(deskRoom.getVirtualDeskUser(), senderUser, deskRoom.getJID(), false));

			// Update virtual desk's presence to new joiner if there is member in the room
			packetList.add(PresenceResponse.createPresenceUpdate(deskRoom.getVirtualDeskUser(), senderUser, deskRoom.getJID(), deskRoom.getDeskPresence()));
		}

		// Notify all members that there is a new joiner
		packetList.addAll(PresenceResponse.createJoinResponse(senderUser, deskRoom.getCurrentMembers(), deskRoom.getJID(), false));

		// Send join response to the new joiner
		Presence responsePresence = (Presence) PresenceResponse.createJoinResponse(senderUser, senderUser, deskRoom.getJID(), deskRoom.isNewlyCreated());
		responsePresence.setID(presence.getID());

		packetList.add(responsePresence);

		deskRoom.addOccupant(senderUser);

		Logger.info("{} has joined the desk [{}] with affiliation: {}",
				new Object[]{senderJID, deskRoom.getJID(), senderUser.getAffiliation()});
	}


	private void sendRecentQuestions(User senderUser) 
	{
		List<UserState> questionList = new ArrayList<UserState>();
		
		Collection<UserState> allQuestions = deskRoom.getAllQuestions();
		
		for (UserState question : allQuestions)
		{
			if (question.getState() == WorkflowState.AwaitResponse)
			{
				questionList.add(question);
			}
		}
		
		int totalQuestions = questionList.size();
		int startIndex = totalQuestions - serviceConfiguration.getNumberOfRecentQuestions();
		
		if (startIndex < 0)
		{
			startIndex = 0;
		}

		StringBuilder recentQuestionMessage = new StringBuilder();
		
		if (startIndex < totalQuestions)
		{
			recentQuestionMessage.append("Questions remain:");
			
			for (int questionIndex = startIndex; questionIndex < totalQuestions; questionIndex++)
			{
				UserState question = questionList.get(questionIndex);
				recentQuestionMessage.append(String.format("\r\n%s \t%s \t%s", question.getPosterNickname(), DateUtils.formatMessageTimestamp(question.getTimestamp()), StringUtils.join(question.getQuestions(), "\r\n")));
			}
			
			packetList.add(MessageResponse.createDeskMessageResponse(recentQuestionMessage.toString(), senderUser, deskRoom.getJID()));
		}
	}
}
