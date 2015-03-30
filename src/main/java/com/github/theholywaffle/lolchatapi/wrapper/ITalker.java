package com.github.theholywaffle.lolchatapi.wrapper;

import com.github.theholywaffle.lolchatapi.LolStatus;

public interface ITalker {

	/**
	 * Gets the name of this talker.
	 * 
	 * @return The name of this Friend or null if no name is assigned.
	 */
	public String getName();

	/**
	 * Gets the name of this friend. If the name was null then we try to fetch
	 * the name with your Riot API Key if provided. Enable forcedUpdate to
	 * always fetch the latest name of this Friend even when the name is not
	 * null.
	 * 
	 * @param forcedUpdate
	 *            True will force to update the name even when it is not null.
	 * @return The name of this Friend or null if no name is assigned.
	 */
	public String getName(boolean forcedUpdate);

	/**
	 * Returns Status object that contains all data when hovering over a friend
	 * inside League of Legends client (e.g. amount of normal wins, current
	 * division and league, queue name, gamestatus,...)
	 * 
	 * @return Status
	 */
	public LolStatus getStatus();

	/**
	 * Gets the XMPPAddress of your Friend (e.g. sum123456@pvp.net)
	 * 
	 * @return the XMPPAddress of your Friend (e.g. sum123456@pvp.net)
	 */
	public abstract String getUserId();

}