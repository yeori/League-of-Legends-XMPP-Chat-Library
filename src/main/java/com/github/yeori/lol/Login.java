package com.github.yeori.lol;

public class Login {
	private String userName;
	private String password;
	
	public Login(String userName, String password) {
		this.userName = userName;
		this.password = "AIR_" + password;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
}
