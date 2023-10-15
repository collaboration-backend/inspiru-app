package com.stc.inspireu.utils;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebexAdapter {

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
}
