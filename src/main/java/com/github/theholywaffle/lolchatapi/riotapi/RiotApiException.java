package com.github.theholywaffle.lolchatapi.riotapi;

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
