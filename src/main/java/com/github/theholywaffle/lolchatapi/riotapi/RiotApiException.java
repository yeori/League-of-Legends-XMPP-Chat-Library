package com.github.theholywaffle.lolchatapi.riotapi;

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
import java.util.HashMap;
import java.util.Map;
/**
 *<pre> 
 * HTTP Status Code	 Reason
 * ---------------- -------------------------------------------------
 *  400	             Bad request
 *  401	             Unauthorized
 *  404	             No summoner data found for any specified inputs
 *  429	             Rate limit exceeded
 *  500	             Internal server error
 *  503	             Service unavailable
 * ---------------- -------------------------------------------------
 *</pre>
 * @author chminseo
 *
 */
public class RiotApiException extends IOException {

	private static final long serialVersionUID = -6622208509416523788L;

	private int responseCode;
	private String reason;
	
	public RiotApiException(int rsCode, String reason, IOException cause) {
		super(cause);
		this.reason = reason;
		this.responseCode = rsCode;
	}
	/**
	 *	<pre> 
	 * HTTP Status Code	 Reason
	 * ---------------- -------------------------------------------------
	 *  400	             Bad request
	 *  401	             Unauthorized
	 *  404	             No summoner data found for any specified inputs
	 *  429	             Rate limit exceeded
	 *  500	             Internal server error
	 *  503	             Service unavailable
	 * ---------------- -------------------------------------------------
	 *</pre>
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}
	
	public String getReason() {
		return reason;
	}
	
	@Override
	public String getMessage() {
		return reason;
	}
	
	@Override
	public String toString() {
		return String.format("[Error Code:%d]%s", responseCode, getMessage()) ;
	}
}
