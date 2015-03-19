package com.github.yeori.lol.muc;

import com.github.theholywaffle.lolchatapi.LolStatus;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApi;
import com.github.theholywaffle.lolchatapi.wrapper.ITalker;

public class MucTalker implements ITalker {

	private String nickName;
	private String jabberId;
	private RiotApi api ;
	
	public MucTalker ( RiotApi api, String nickName, String jabberId) {
		this.api = api;
		this.nickName = nickName;
		this.jabberId = jabberId;
	}
	@Override
	public String getName() {
		return nickName;
	}

	@Override
	public String getName(boolean forcedUpdate) {
		return nickName;
	}

	@Override
	public LolStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserId() {
		// TODO Auto-generated method stub
		return null;
	}

}
