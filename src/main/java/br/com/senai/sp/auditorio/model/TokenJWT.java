package br.com.senai.sp.auditorio.model;

import lombok.Data;


public class TokenJWT {
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
