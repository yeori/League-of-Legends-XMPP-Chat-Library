package com.github.yeori.lol.listeners;

import com.github.yeori.lol.muc.ChatRoom;

/**
 * 
 * @author chminseo
 *
 */
public interface ChatRoomListener {
	
	public void roomCreated(ChatRoom room) ;
	public void joinedToChatRoom(ChatRoom room);
	
}
