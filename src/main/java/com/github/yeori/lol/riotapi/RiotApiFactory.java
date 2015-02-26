package com.github.yeori.lol.riotapi;

import com.github.theholywaffle.lolchatapi.ChatServer;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApi;
import com.github.theholywaffle.lolchatapi.riotapi.RiotApiKey;

public interface RiotApiFactory {
	
	/**
	 * create riot api instance
	 * @param riotKey - you development key issued from https://developer.riotgames.com/ 
	 * @param server - server info(host, api url etc)
	 * @return
	 */
	RiotApi createRiotApi(RiotApiKey riotKey, ChatServer server);

}
