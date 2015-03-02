package com.github.yeori.lol.muc;

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


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LolRoomNaming implements IRoomNaming {
	
	private MessageDigest md ;
	private String prefix ;
	private String domain ;
	
	public LolRoomNaming(String prefix, String domain) throws NoSuchAlgorithmException {
		md = MessageDigest.getInstance("SHA1");
		this.prefix = prefix;
		this.domain = domain;
	}
	
	/**
	 * translates a bare(plain) room name to a lol-specific form.
	 * 
	 * ex) "lol" is translated to "pu~" + SHA1("lol")
	 */
	@Override
	public String translate(String bareRoomName) {
		
		byte[] result = md.digest(bareRoomName.getBytes());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return prefix + sb.toString() +"@" + domain;
	}
}