package com.github.yeori.lol.muc;

import org.jivesoftware.smackx.muc.MultiUserChat;

public class ChatRoom {
	private MultiUserChat mucSource;
	public ChatRoom() {
	}

	public ChatRoom(MultiUserChat muc) {
		mucSource = muc;
	}

}
