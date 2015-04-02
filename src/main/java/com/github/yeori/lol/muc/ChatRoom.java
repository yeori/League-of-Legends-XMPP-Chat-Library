package com.github.yeori.lol.muc;

/*
 * #%L
 * League of Legends XMPP Chat Library
 * %%
 * Copyright (C) 2014 - 2015 Bert De Geyter
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.JDOMException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.lolchatapi.ChatMode;
import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.LolStatus;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApi;
import com.github.yeori.lol.listeners.MucListener;
/**
 * 공개 또는 비공개 채팅방을 나타내는 클래스
 * 
 * @author chminseo
 *
 */
public class ChatRoom {
	private Logger logger = LoggerFactory.getLogger(ChatRoom.class);
	private LolChat lol;
	private String roomId ;
	private MultiUserChat mucSource;
	final private ArrayList<MucListener> mucListeners = new ArrayList<>();
	final private List<Talker> talkers = new ArrayList<>();
	
	public ChatRoom(LolChat lol, MultiUserChat muc, String roomId) {
		this.lol = lol;
		this.mucSource = muc;
		this.roomId = roomId;
		
		installListeners();
	}
	
	final private void installListeners() {
		mucSource.addMessageListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) throws NotConnectedException {
				// TODO 공개 채팅방 메세지 처리
				Message msg = Message.class.cast(packet);
				logger.info(String.format("[MUC 메세지] %s", msg));
				notifyMessageReceived(msg);
			}
		});
		
		mucSource.addParticipantListener(new PacketListener() {
			
			
			@Override
			public void processPacket(Packet packet) throws NotConnectedException {
				Presence psc = Presence.class.cast(packet);
				
				String roomJID = psc.getFrom(); 
				String nickName = StringUtils.parseResource(roomJID);
				if ( StringUtils.isNullOrEmpty(nickName)) {
					logger.warn("[INVALID NICKNAME] nickname 부분이 없음." + packet.toXML());
					return ;
				}
				
				Talker talker = findTalker(nickName);
				if ( talker == null ) {
					registerTalker(psc);
					return ;
				}
				if ( psc.getType() == Type.unavailable) {
					unregisterTalker(talker);
					return ;
				}
				notifyTalkerMode ( talker, psc.getMode() );
				
			}
		});
	}

	/**
	 * 새로운 참여자로 등록함.
	 * @param psc
	 */
	final private void registerTalker ( Presence psc) {
		/* 공개 채팅방에서의 jid의 형태 pu~xxxxxxx@lvl.pvp.net/[UserNickname]
		 *       roomJID       |/| nickname
		 * --------------------+--------------
		 * pu~xxx@lvl.pvp.net  |/| [user-nickname]
		 */
		String roomJID = psc.getFrom();
		String nickName = StringUtils.parseResource(roomJID);
		
		MUCUser mucUserPacket = findMUCUserExtension(psc);
		String talkerJID = null; // full JID
		if ( mucUserPacket == null ) {					
			if ( nickName.length() > 0 ) {
				// 맨처음 방에 들어갔을때 전달되는 메세지에는
				// nickname 부분이 없음.
				RiotApi riot = lol.getRiotApi();
				try {
					talkerJID = "sum" + riot.getSummonerId(nickName) + "@pvp.net";
				} catch (IOException | URISyntaxException e) {
					logger.error(String.format("fail to read summoner id using [%s], set to zero. (%s)", 
							nickName,
							psc), e);
				}
			}
		} else {
			talkerJID = mucUserPacket.getItem().getJid();
		}
		
		Talker talker = new Talker(talkerJID, nickName, ChatRoom.this);
		
		String statusXML = psc.getStatus();
		if (StringUtils.isNotEmpty(statusXML)) {
			// 로그인한 자기 자신인 경우에는 status element가 없음.
			try {
				talker.setStatus(new LolStatus(statusXML));
			} catch (JDOMException e) {
				logger.warn("[STATUS ERROR] fail to parse LolStatus from <presence>.<status>", e);
			} catch (IOException e) {
				logger.warn("[IO ERROR] fail to parse LolStatus", e);
			}
		}
		talkers.add(talker);
		
		notifyNewTalkerEntrance(talker);
		
		logger.info(String.format("[참여자:roomJID(%s):nick(%s):summonID(%s)] %s is %s, and mode is %s",
				roomJID,
				talker.getNickName(),
				talker.getUserId(),
				psc.getFrom(), 
				psc.getType(), 
				psc.getMode()) );
	}
	
	/**
	 * 채팅방에 들어온 새로운 참여자
	 * @param talker
	 */
	private void notifyNewTalkerEntrance(Talker talker) {
		ArrayList<MucListener> cloned = null;
		
		synchronized (mucListeners) {			
			cloned = new ArrayList<>(mucListeners);
		}
		
		for ( int i = 0 ; i < cloned.size() ; i++) {
			cloned.get(i).newTalkerEntered(this, talker);
		}
	}
	/**
	 * 사용자의 상태정보 통보
	 * @param mode
	 */
	private void notifyTalkerMode(Talker talker, Mode mode) {
		ArrayList<MucListener> cloned = null;
		
		synchronized (mucListeners) {
			cloned = new ArrayList<>(mucListeners);
		}
		ChatMode chatMode  = ChatMode.AVAILABLE;
		
		if ( mode == Mode.away) {
			chatMode = ChatMode.AWAY;
		} else if ( mode == Mode.chat) {
			chatMode = ChatMode.AVAILABLE;
		} else if ( mode == Mode.dnd || mode == Mode.xa) {
			chatMode = ChatMode.BUSY;
		}
		
		for ( int i = 0 ; i < cloned.size() ; i++) {
			cloned.get(i).chatModeChanged(
					talker.getRoom(), 
					talker, 
					chatMode);
		}
	}

	final MUCUser findMUCUserExtension(Presence psc) {
		PacketExtension pe = psc.getExtension("http://jabber.org/protocol/muc#user");
		return pe.getClass() == MUCUser.class ? MUCUser.class.cast(pe) : null;
	}
	
	/**
	 * 참여자 제거
	 * @param psc
	 */
	final private void unregisterTalker ( Talker talker) {
		for ( Talker t : talkers) {
			if ( t.equals(talker)) {
				talkers.remove(talker);
				return ;
			}
		}
	}

	public void addMucListener ( MucListener listener) {
		if (mucListeners.contains(listener)) {
			mucListeners.remove(listener);
		}
		mucListeners.add(listener);
	}
	
	public void removeMucListener( MucListener listener) {
		mucListeners.remove(listener);
	}
	
	/**
	 * finds a talker by nickname in the chatroot
	 * @param nickName
	 */
	public Talker findTalker ( String nickName) {
		for( Talker talker : talkers) {
			if ( talker.getName().equals(nickName) ) {
				return talker;
			}
		}
		return null;
	}
	void notifyMessageReceived(Message msg) {
		
		String fqJID = msg.getFrom();
		String roomDomain = StringUtils.parseBareAddress(fqJID);
		String nickName = StringUtils.parseResource(fqJID);
		
		Talker talker = (Talker)findTalker ( nickName);
		logger.debug(String.format("[MESSAGE][ROOM:%s, nick:%s, talker:%s] %s", roomDomain, nickName, talker, msg));
		String body = msg.getBody();

		List<MucListener> listeners ;
		synchronized (mucListeners) {
			listeners = new ArrayList<>(mucListeners);
		}
		for( int i = 0 ; i < listeners.size() ; i++ ) {
			listeners.get(i).onMucMessage(talker, body);
		}
	}

	public void sendMessage(String message) throws MucException{
		try {
			mucSource.sendMessage(message);
		} catch (NotConnectedException | XMPPException e) {
			throw new MucException("fail to send message : " + message, e);
		}
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomId == null) ? 0 : roomId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatRoom other = (ChatRoom) obj;
		if (roomId == null) {
			if (other.roomId != null)
				return false;
		} else if (!roomId.equals(other.roomId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChatRoom [room:" + roomId + "] mucSource=" + mucSource
				+ ", talkers=" + talkers + "]";
	}
	
	
	
}
