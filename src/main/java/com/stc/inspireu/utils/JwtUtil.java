package com.stc.inspireu.utils;

import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Value("${jwt.assets.secret}")
	private String assetsSecret;

	@Value("${jwt.assets.expiration}")
	private long assetsExpiration;

	@Value("${jwt.auth.secret}")
	private String secret;

	@Value("${jwt.auth.expiration}")
	private long expiration;

	@Value("${jwt.generic.secret}")
	private String genericSecret;

	@Value("${jwt.generic.expiration}")
	private long genericExpiration;

	public Map<String, Object> getClaimsFromAuthToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);

			JWTVerifier verifier = JWT.require(algorithm).build();

			DecodedJWT jwt = verifier.verify(token);

			String encodedString = jwt.getPayload();

			byte[] decodedBytes = Base64.getDecoder().decode(encodedString);

			String decodedString = new String(decodedBytes);

			JSONObject jsonObject = new JSONObject(decodedString);

			Map<String, Object> data = jsonObject.toMap();

			return data;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	public Map<String, Object> getClaimsFromGenericToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(genericSecret);

			JWTVerifier verifier = JWT.require(algorithm).build();

			DecodedJWT jwt = verifier.verify(token);

			String encodedString = jwt.getPayload();

			byte[] decodedBytes = Base64.getDecoder().decode(encodedString);

			String decodedString = new String(decodedBytes);

			JSONObject jsonObject = new JSONObject(decodedString);

			Map<String, Object> data = jsonObject.toMap();

			return data;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	private Date generateExpirationDate(long expiration) {
		return new Date(System.currentTimeMillis() + expiration);
	}

	public String generateAuthToken(Map<String, Object> claims) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			String token = JWT.create().withExpiresAt(generateExpirationDate(expiration)).withPayload(claims)
					.sign(algorithm);
			return token;
		} catch (JWTCreationException e) {
			LOGGER.error(e.getMessage());
			return "invalid_token";
		}
	}

	public String genericJwtToken(Map<String, Object> claims, long... expiration) {

		long exp = expiration.length > 0 ? expiration[0] : genericExpiration;

		try {
			Algorithm algorithm = Algorithm.HMAC256(genericSecret);
			String token = JWT.create().withExpiresAt(generateExpirationDate(exp)).withPayload(claims).sign(algorithm);
			return token;
		} catch (JWTCreationException e) {
			LOGGER.error(e.getMessage());
			return "invalid_token";
		}
	}

	public String generateAssetJwtToken(Map<String, Object> claims, Boolean... sameExpiryAsAuthToken) {
		try {

			long ex = sameExpiryAsAuthToken.length > 0 ? expiration : assetsExpiration;

			Algorithm algorithm = Algorithm.HMAC256(assetsSecret);
			String token = JWT.create().withExpiresAt(generateExpirationDate(ex)).withPayload(claims).sign(algorithm);
			return token;
		} catch (JWTCreationException e) {
			LOGGER.error(e.getMessage());
			return "invalid_token";
		}
	}

	public Map<String, Object> getClaimsFromAssetToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(assetsSecret);

			JWTVerifier verifier = JWT.require(algorithm).build();

			DecodedJWT jwt = verifier.verify(token);

			String encodedString = jwt.getPayload();

			byte[] decodedBytes = Base64.getDecoder().decode(encodedString);

			String decodedString = new String(decodedBytes);

			JSONObject jsonObject = new JSONObject(decodedString);

			Map<String, Object> data = jsonObject.toMap();

			return data;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}
	}

	public String generateZoomJwtToken(Map<String, Object> claims, long expInMilliSec, String secret) {

		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);

			String token = JWT.create().withIssuer((String) claims.get("iss"))
					.withExpiresAt(new Date(System.currentTimeMillis() + expInMilliSec)).sign(algorithm);

			return token;
		} catch (JWTCreationException e) {
			LOGGER.error(e.getMessage());
			return "invalid_token";
		}

	}

}
