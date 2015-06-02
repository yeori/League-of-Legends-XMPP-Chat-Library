package com.github.theholywaffle.lolchatapi;

/*
 * #%L
 * League of Legends XMPP Chat Library
 * %%
 * Copyright (C) 2014 Bert De Geyter
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.jdom2.JDOMException;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.lolchatapi.listeners.FriendListener;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;
import com.github.theholywaffle.lolchatapi.wrapper.Friend.FriendStatus;

public class LeagueRosterListener implements RosterListener {
	private Logger logger = LoggerFactory.getLogger(LeagueRosterListener.class);
	
	private final HashMap<String, Presence.Type> typeMap = new HashMap<>();
	private final HashMap<String, Presence.Mode> modeMap = new HashMap<>();
	private final HashMap<String, LolStatus> lolStatusMap = new HashMap<>();
	private final HashMap<String, FriendStatus> friendStatusUsers = new HashMap<>();
		
	private final LolChat api;

	private boolean added;
	private final XMPPConnection connection;
	
	public LeagueRosterListener(LolChat api, XMPPConnection connection) {
		this.api = api;
		this.connection = connection;
	}

	public void entriesAdded(Collection<String> e) {
		Friend f;
		for (final String jid : e) {
			logger.debug(String.format("[ADDED ENTRY]%s", jid));
			f = api.getFriendById(jid);
			if ( added ) {
				onNewFriend(f);
			}
			
			if (!added && !api.isLoaded()) {
				if (f.isOnline()) {
					typeMap.put(jid, Presence.Type.available);
					modeMap.put(jid, f.getChatMode().mode);
					lolStatusMap.put(jid, f.getStatus());
				} else {
					typeMap.put(jid, Presence.Type.unavailable);
				}
			}
			if (f.getGroup() == null) {
				api.getDefaultFriendGroup().addFriend(f);
			}

			if (f.getFriendStatus() != FriendStatus.MUTUAL_FRIENDS) {
				friendStatusUsers.put(jid, f.getFriendStatus());
			}
		}
		
		synchronized (this) {
			added = true;
			this.notifyAll();
		}
	}

	public void entriesDeleted(Collection<String> entries) {
		for (final String jid : entries) {
			logger.debug(String.format("[DELETED ENTRY]%s", jid));
//			friendStatusUsers.put(s, null); /* delete해야 하는거 아닌지? */
			friendStatusUsers.remove(jid);
			for (final FriendListener l : api.getFriendListeners()) {
				/* COMMENT roster entry와 friend가 삭제된 후에 호출되기 때문에
				 * jid를 이용해서 굳이 또 name을 읽어들일 필요는 없을 듯.
				 * 아래와같이 name은 empty string으로 대체함.
				 * 
				 */
				l.onRemoveFriend(jid, "");
			}
		}
	}

	public void entriesUpdated(Collection<String> e) {
		for (final String s : e) {
			final Friend f = api.getFriendById(s);
			logger.debug(String.format("[UPDATED FRIEND]%s", f.toString()));
			final FriendStatus previous = friendStatusUsers.get(s);
//			if (((previous != null && previous != FriendStatus.MUTUAL_FRIENDS)
//					|| previous == null || !api.isLoaded())
//					&& f.getFriendStatus() == FriendStatus.MUTUAL_FRIENDS) {
//				onNewFriend(f);
//			}
			friendStatusUsers.put(s, f.getFriendStatus());
		}
	}

	public boolean isLoaded() {
		return added;
	}

	private void onNewFriend(Friend f) {
		for (final FriendListener l : api.getFriendListeners()) {
			l.onNewFriend(f);
		}
	}

	public void presenceChanged(Presence p) {
		String from = p.getFrom();
		logger.debug(String.format("[PRESENCE CHAGED] %s", p.toXML().toString()));
		if (from != null) {
			p = connection.getRoster().getPresence(p.getFrom());
			from = StringUtils.parseBareAddress(from);
			final Friend friend = api.getFriendById(from);
			if (friend == null) {
				return ;
			}
			for (final FriendListener l : api.getFriendListeners()) {
				final Presence.Type previousType = typeMap.get(from);
				if (p.getType() == Presence.Type.available
						&& (previousType == null || previousType != Presence.Type.available)) {
					l.onFriendJoin(friend);
				} else if (p.getType() == Presence.Type.unavailable
						&& (previousType == null || previousType != Presence.Type.unavailable)) {
					l.onFriendLeave(friend);
				}

				final Presence.Mode previousMode = modeMap.get(from);
				if (p.getMode() == Presence.Mode.chat
						&& (previousMode == null || previousMode != Presence.Mode.chat)) {
					l.onFriendAvailable(friend);
				} else if (p.getMode() == Presence.Mode.away
						&& (previousMode == null || previousMode != Presence.Mode.away)) {
					l.onFriendAway(friend);
				} else if (p.getMode() == Presence.Mode.dnd
						&& (previousMode == null || previousMode != Presence.Mode.dnd)) {
					l.onFriendBusy(friend);
				}

				if (p.getStatus() != null) {
					try {
						final LolStatus previousStatus = lolStatusMap
								.get(from);
						final LolStatus newStatus = new LolStatus(
								p.getStatus());
						if (previousStatus != null
								&& !newStatus.equals(previousStatus)) {
							l.onFriendStatusChange(friend);
						}
					} catch (JDOMException | IOException e) {
					}
				}
			}

			typeMap.put(from, p.getType());
			modeMap.put(from, p.getMode());
			if (p.getStatus() != null) {
				try {
					lolStatusMap.put(from, new LolStatus(p.getStatus()));
				} catch (JDOMException | IOException e) {
				}
			}
		}
	}

	/**
	 * makes caller thread wait while entries loaded 
	 */
	public void waitForLoaded() {
		String tname = Thread.currentThread().getName();
		synchronized (this) {
			while ( !added ) {
				logger.debug("wating for entry to be loaded... t: " + tname);				
				try {
					this.wait();
				} catch (InterruptedException e) {
					logger.debug("awakend by interrupt");
				}
			}
			this.notifyAll();
		}
		logger.debug(String.format("all entries loaded. [t: %s]", tname));
	}

}
