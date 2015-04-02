package com.github.theholywaffle.lolchatapi.wrapper;

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