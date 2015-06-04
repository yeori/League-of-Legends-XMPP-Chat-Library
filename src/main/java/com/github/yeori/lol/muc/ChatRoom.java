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
	private String roomName ;
	private MultiUserChat mucSource;
	final private ArrayList<MucListener> mucListeners = new ArrayList<>();
	final private List<Talker> talkers = new ArrayList<>();
	private boolean publicity;
	
	public ChatRoom(LolChat lol, MultiUserChat muc, String roomName) {
		this( lol, muc, roomName, true);
	}
	
	public ChatRoom(LolChat lol, MultiUserChat muc, String roomName,
			boolean isPublic) {
		this.lol = lol;
		this.mucSource = muc;
		this.roomName = roomName;
		this.publicity = isPublic;
		installListeners();
	}

	public String getRoomName() {
		return roomName;
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
		Talker talker = createTalker(psc);
		addTalker(talker);
		
		logger.info(String.format("[참여자:roomJID(%s):nick(%s):summonID(%s)] %s is %s, and mode is %s",
				roomJID,
				talker.getNickName(),
				talker.getUserId(),
				psc.getFrom(), 
				psc.getType(), 
				psc.getMode()) );
	}
	
	final private Talker createTalker ( Presence psc) {
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
		
		Talker talker = new Talker(talkerJID, nickName, this);
		
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
		
		return talker;
	}
	
	final MUCUser findMUCUserExtension(Presence psc) {
		PacketExtension pe = psc.getExtension("http://jabber.org/protocol/muc#user");
		return pe.getClass() == MUCUser.class ? MUCUser.class.cast(pe) : null;
	}
	
	/**
	 * 채팅방에 들어온 새로운 참여자를 리스너에 통보
	 * @param talker
	 */
	private void notifyNewTalkerEntrance(Talker talker) {
		ArrayList<MucListener> cloned = null;
		
		synchronized (mucListeners) {			
			cloned = new ArrayList<>(mucListeners);
			cloned.addAll(lol.getMultiUserChatListener());
		}
		
		for ( int i = 0 ; i < cloned.size() ; i++) {
			cloned.get(i).newTalkerEntered(this, talker);
		}
	}
	
	/**
	 * 채팅방에 올라온 새로운 메세지를 리스너에 통보
	 * @param msg
	 */
	private void notifyMessageReceived(Message msg) {
		
		String fqJID = msg.getFrom();
		String roomDomain = StringUtils.parseBareAddress(fqJID);
		String nickName = StringUtils.parseResource(fqJID);
		
		Talker talker = (Talker)findTalker ( nickName);
		logger.debug(String.format("[MESSAGE][ROOM:%s, nick:%s, talker:%s] %s", roomDomain, nickName, talker, msg));
		String body = msg.getBody();

		List<MucListener> listeners ;
		synchronized (mucListeners) {
			listeners = new ArrayList<>(mucListeners);
			listeners.addAll(lol.getMultiUserChatListener());
		}
		for( int i = 0 ; i < listeners.size() ; i++ ) {
			try {
				listeners.get(i).onMucMessage(talker, body);				
			} catch ( Exception e) {
				logger.error(String.format("[MUC LISTENER ERROR]", listeners.get(i)));
			}
		}
	}

	/**
	 * 채팅방에 참가중인 참여자의 상태정보를 리스너에 통보(게임관전, 게임 참여 등 참여자 모드 변경시 통보됨)
	 * @param mode
	 */
	private void notifyTalkerMode(Talker talker, Mode mode) {
		ArrayList<MucListener> cloned = null;
		
		synchronized (mucListeners) {
			cloned = new ArrayList<>(mucListeners);
			cloned.addAll(lol.getMultiUserChatListener());
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
	
	/**
	 * 채팅방을 나갔을때 리스너에게 통보
	 * @param talker
	 */
	private void notifyTalkerLeaved(Talker talker) {
		ArrayList<MucListener> cloned = null;
		
		synchronized (mucListeners) {			
			cloned = new ArrayList<>(mucListeners);
			cloned.addAll(lol.getMultiUserChatListener());
		}
		
		for ( int i = 0 ; i < cloned.size() ; i++) {
			cloned.get(i).talkerLeaved(talker.getRoom(), talker);
		}
	}
	
	/**
	 * 참여자를 채팅방에서 제거함
	 * @param psc
	 */
	final private void unregisterTalker ( Talker talker) {
		for ( Talker t : talkers) {
			if ( t.equals(talker)) {
				talkers.remove(talker);
				notifyTalkerLeaved(talker);
				logger.debug("[TALKER LEAVED]" + talker + " count : " + this.countTalkers());
				return ;
			}
		}
	}

	/**
	 * 채팅방 리스너를 등록함.
	 * @param listener
	 */
	public void addMucListener ( MucListener listener) {
		if (mucListeners.contains(listener)) {
			mucListeners.remove(listener);
		}
		mucListeners.add(listener);
	}
	
	/**
	 * 채팅방 리스너를 제거
	 * @param listener
	 */
	public void removeMucListener( MucListener listener) {
		mucListeners.remove(listener);
	}
	
	/**
	 * 새로운 채팅 참여자를 등록함.
	 * @param newTalker
	 */
	public void addTalker( Talker newTalker) {
		talkers.add(newTalker);
		notifyNewTalkerEntrance(newTalker);
		logger.debug("[TALKER JOINED]" + newTalker + " count : " + this.countTalkers());
	}
	/**
	 * finds a talker by nickname in the chatroom
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
	
	/**
	 * 현재 채팅방에 등록된 모든 참가자들을 반환
	 * @return
	 */
	public List<Talker> getTalkers() {
		return new ArrayList<>(talkers);
	}
	
	/**
	 * 채팅방 참여자의 숫자
	 * @return
	 */
	public int countTalkers() {
		return talkers.size();
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
		result = prime * result + ((roomName == null) ? 0 : roomName.hashCode());
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
		if (roomName == null) {
			if (other.roomName != null)
				return false;
		} else if (!roomName.equals(other.roomName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChatRoom [room:" + roomName + "] mucSource=" + mucSource
				+ ", talkers=" + talkers + "]";
	}
	/**
	 * 비공개 채팅방이면 true반환
	 */
	public boolean isPrive() {
		return ! publicity;
	}
	/**
	 * 공개 채팅방이면 true를 반환.
	 */
	public boolean isPublic() {
		return publicity;
	}
}
