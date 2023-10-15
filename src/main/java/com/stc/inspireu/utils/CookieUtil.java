package com.stc.inspireu.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

	public void setCookie(HttpServletResponse response, String name, String value) {
		Cookie c = new Cookie(name, value);
		c.setPath("/");
		response.addCookie(c);
	}

	public String getCookie(HttpServletRequest request, String name) {

		String localval = null;

		Cookie[] cookies = getCookies(request, name);

		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(name)) {
					localval = cookies[i].getValue();
				}
			}
		}
		return localval;
	}

	public Cookie[] getCookies(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		return cookies;
	}
}
