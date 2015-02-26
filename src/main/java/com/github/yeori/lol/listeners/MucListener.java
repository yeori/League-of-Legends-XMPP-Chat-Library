package com.github.yeori.lol.listeners;

import com.github.theholywaffle.lolchatapi.LolChat;
import com.github.theholywaffle.lolchatapi.wrapper.Friend;

public interface MucListener {

	/**
	 * 
	 * @param roomName
	 * @param inviter
	 * @param password
	 * @return 
	 */
	public boolean invitationReceived(LolChat chatApi, String roomName, String inviter, String password) ;

	public void onMucMessage(Friend sender, String body);
	
}
