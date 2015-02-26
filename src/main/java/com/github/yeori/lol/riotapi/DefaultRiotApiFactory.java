package com.github.yeori.lol.riotapi;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApi;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;

public class DefaultRiotApiFactory implements RiotApiFactory {

	public DefaultRiotApiFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public RiotApi createRiotApi(RiotApiKey riotKey, ChatServer server) {
		RiotApi api = null;
		if (riotKey != null && server.api != null) {
			// TODO should be replaced properly
			api = RiotApi.build(riotKey, server);
		}
		return api;
	}

}
