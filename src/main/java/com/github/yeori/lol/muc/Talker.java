package com.github.yeori.lol.muc;

import com.github.theholywaffle.lolchatapi.LolStatus;
import com.github.theholywaffle.lolchatapi.wrapper.ITalker;

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


public class Talker implements ITalker{

	private String summonerJID ;
	private String nickName;
	private ChatRoom room;
	private LolStatus status;
	
	Talker(String summonerJID, String nickName, ChatRoom room) {
		super();
		this.summonerJID = summonerJID;
		this.nickName = nickName;
		this.room = room;
	}

	public String getNickName() {
		return nickName;
	}

	public ChatRoom getRoom() {
		return room;
	}

	@Override
	public String toString() {
		return "Talker [summoner=" + summonerJID + ", nick=" + nickName
				+ " at " + room + "]";
	}

	@Override
	public String getName() {
		return getNickName();
	}

	@Override
	public String getName(boolean forcedUpdate) {
		return getNickName();
	}

	@Override
	public LolStatus getStatus() {
		return status;
	}
	public void setStatus(LolStatus newStatus) {
		LolStatus oldStatus = status;
		status = newStatus;
	}

	@Override
	public String getUserId() {
		return summonerJID;
	}

	
	
}
