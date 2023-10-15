package com.stc.inspireu.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

//import org.springframework.security.crypto.bcrypt.BCrypt;

@Component
public class PasswordUtil {

	public String getHashedPassword(String password) {
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
		return hashedPassword;
	}

	public boolean verifyPassword(String password, String hashedPassword) {
		boolean isSame = BCrypt.checkpw(password, hashedPassword);
		return isSame;
	}
}
