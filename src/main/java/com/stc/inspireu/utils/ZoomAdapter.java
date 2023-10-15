package com.stc.inspireu.utils;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ZoomAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Value("${zoom.api.url}")
	private String zoomApiUrl;

	@Value("${zoom.api.key}")
	private String zoomApiKey;

	@Value("${zoom.api.secret}")
	private String zoomApiSecret;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private Utility utility;

	@Autowired
	private RestClient restClient;

	public String getZoomAccessToken() {

		Map<String, Object> claims = new HashMap<>();

		claims.put("iss", zoomApiKey);

		return jwtUtil.generateZoomJwtToken(claims, 3600 * 1000, zoomApiSecret);
	}

	public JsonNode createZoomMeeting(Date startDate, int duration) {

		HttpHeaders httpHeaders = new HttpHeaders();

		httpHeaders.add("Authorization", "Bearer " + getZoomAccessToken());

		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
		df.setTimeZone(tz);
		String nowAsISO = df.format(startDate);

		JSONObject jSONObject = new JSONObject();
		jSONObject.put("topic", "test meeting" + System.currentTimeMillis());
		jSONObject.put("type", 2);
		jSONObject.put("start_time", nowAsISO);
		jSONObject.put("duration", duration);
		jSONObject.put("password", utility.getAlhpaNumeric(10));

		JsonNode jsonNode = restClient.invoke("post", zoomApiUrl + "/users/me/meetings", httpHeaders, jSONObject);

		return jsonNode;
	}

}
