package com.stc.inspireu.utils;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class SMSUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Value("${sms.opt.environment}")
	private String environment;

	@Value("${sms.opt.username}")
	private String username;

	@Value("${sms.opt.password}")
	private String password;

	@Value("${sms.opt.algorithmName}")
	private String algorithmName;

	@Value("${sms.opt.host}")
	private String host;

	@Value("${sms.opt.port}")
	private String port;

	@Value("${sms.opt.sendUrl}")
	private String sendUrl;

	@Value("${sms.opt.validateUrl}")
	private String validateUrl;

	@Value("${sms.opt.locale}")
	private String locale;

	class DatapowerConstants {
		public static final String DATE_HEADER = "Date";
		public static final String CLIENT_HEADER = "x-igate-client";
		public static final String X_SERVICE_ID = "X-SERVICE-ID";
		public static final String HUB_PRIVATE_CLIENT = "INSPIREU";
		public static final String DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
		public static final String TIMEZONE = "GMT";
		public static final String CLIENT_VERSION = "8.1";
		public static final String CONTENT_TYPE_TEXT_JSON = "text/json";
		public static final String REST_HOST = "x-igate-API";
		public static final String CONTENT_TYPE = "content-type";
		public static final String HOST = "host";
		public static final String USER_AGENT = "useragent";
		public static final String VERSION = "version";
		public static final String API_VERSION_1 = "1.0";
	}

	class ServiceURL {

		private String apiValue;
		private HttpMethod httpMethod;
		private String client;
		private String host;
		private String port;
		private String locale = "en";
		private String version = "v1.0";
		private String clientName = DatapowerConstants.HUB_PRIVATE_CLIENT;

		public String getApiValue() {
			return apiValue;
		}

		public void setApiValue(String apiValue) {
			this.apiValue = apiValue;
		}

		public HttpMethod getHttpMethod() {
			return httpMethod;
		}

		public void setHttpMethod(HttpMethod httpMethod) {
			this.httpMethod = httpMethod;
		}

		public String getClient() {
			return client;
		}

		public void setClient(String client) {
			this.client = client;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getLocale() {
			return locale;
		}

		public void setLocale(String locale) {
			this.locale = locale;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getClientName() {
			return clientName;
		}

		public void setClientName(String clientName) {
			this.clientName = clientName;
		}

		public String getServiceUrl() {
			return getHost() + ":" + getPort() + getApiValue().replace("{locale}", locale);
		}
	}

	public String sendOtp(String phoneNumber) {

		try {
			JSONObject json = new JSONObject();

			json.put("destination", phoneNumber.replaceFirst("^0+(?!$)", ""));

			LOGGER.info("1: " + json.toString());

			String response = execute(String.class, sendUrl, HttpMethod.POST, locale, json.toString());

			LOGGER.info("2: " + response);
			return response;
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return "error";
		}

	}

	public String validateOtp(String id, String otp) {

		try {
			JSONObject json = new JSONObject();

			json.put("id", id);
			json.put("otp", otp);

			LOGGER.info("1: " + json.toString());

			String response = execute(String.class, validateUrl, HttpMethod.PUT, locale, json.toString());

			LOGGER.info("2: " + response);
			return "ok";
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			return "error";
		}

	}

	private String execute(Class<String> responseClass, String apiUrl, HttpMethod method, String locale, String body) {

		try {
			ServiceURL service = buildServiceUrl(apiUrl, locale, method);

			LOGGER.info("3: " + service.getApiValue() + " " + service.getClient() + " " + service.getClientName() + " "
					+ service.getHost() + " " + service.getLocale() + " " + service.getPort() + " "
					+ service.getServiceUrl() + " " + service.getVersion() + " " + service.getHttpMethod().toString());

			MultiValueMap<String, String> clientHeaders = generatePublicHeaders(service, locale);

			HttpHeaders headers = new HttpHeaders();

			headers.setAccept(Collections.singletonList(MediaType.ALL));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setContentLength(body.getBytes().length);
			headers.set("host", host + ":" + port);
			headers.set("user-agent", DatapowerConstants.HUB_PRIVATE_CLIENT);

			headers.addAll(clientHeaders);

			LOGGER.info("4: ");

			for (Map.Entry<String, List<String>> h : headers.entrySet()) {

				for (String s : h.getValue()) {
					LOGGER.info(h.getKey() + " ============ " + s);
				}
			}

			HttpEntity<Object> entity = new HttpEntity<>(body, headers);

			String serviceUrl = service.getServiceUrl();

			LOGGER.info("5: " + serviceUrl);

			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, null, null);
			CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context).build();
			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//			RestTemplate restTemplate = new RestTemplate(factory);

			RestTemplate restTemplate = new RestTemplate(factory);

			ResponseEntity<String> restResponse = restTemplate.exchange(serviceUrl, service.getHttpMethod(), entity,
					responseClass);

			LOGGER.info("555: " + restResponse.getStatusCodeValue());

			return restResponse.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}

	ServiceURL buildServiceUrl(String url, String locale, HttpMethod httpMethod) {
		ServiceURL surl = new ServiceURL();
		surl.setLocale(locale);
		surl.setHttpMethod(httpMethod);
		surl.setApiValue(url);
		surl.setHost(host);
		surl.setPort(port);
		return surl;
	}

	MultiValueMap<String, String> generatePublicHeaders(ServiceURL serviceURL, String locale) {
		MultiValueMap<String, String> clientHeaders = new LinkedMultiValueMap<>();
		String timeInGMT = getTimeInGMT();
		String xIgateClient = generatePublicClient(serviceURL, timeInGMT, locale);
		clientHeaders.add(DatapowerConstants.DATE_HEADER, timeInGMT);
		clientHeaders.add(DatapowerConstants.CLIENT_HEADER, xIgateClient);
		clientHeaders.add(DatapowerConstants.X_SERVICE_ID, "WEBAPP");

		return clientHeaders;
	}

	String generatePublicClient(ServiceURL serviceURL, String timeInGMT, String locale) {

		String userAgent = serviceURL.getClientName();
		String serviceUrl = serviceURL.getApiValue().replace("{locale}", locale);
		String methodType = serviceURL.getHttpMethod().name();
		String version = serviceURL.getVersion();

		String sharedSecret = username + ":" + password;
		String hash = methodType + serviceUrl + locale + timeInGMT + userAgent + version + methodType;

		LOGGER.info("6: " + sharedSecret);
		LOGGER.info("7: " + hash);

		try {
			String hashString = generateHmac(hash, sharedSecret, algorithmName);

			LOGGER.info("8: " + hashString);

			String publicClient = hashString + "\\" + DatapowerConstants.CONTENT_TYPE + ":"
					+ DatapowerConstants.CONTENT_TYPE_TEXT_JSON + "\\" + DatapowerConstants.HOST + ":"
					+ DatapowerConstants.REST_HOST + "\\" + DatapowerConstants.USER_AGENT + ":" + userAgent + "/"
					+ DatapowerConstants.CLIENT_VERSION + "\\" + DatapowerConstants.VERSION + ":"
					+ DatapowerConstants.API_VERSION_1;

			LOGGER.info("9: " + publicClient);

			return publicClient;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	String getTimeInGMT() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(DatapowerConstants.DATE_PATTERN, Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone(DatapowerConstants.TIMEZONE));
		String strTimeInGMT = dateFormat.format(calendar.getTime());
		LOGGER.info("10: " + strTimeInGMT);
		return strTimeInGMT;
	}

	String generateHmac(String hashText, String userPass, String algorithmName)
			throws NoSuchAlgorithmException, InvalidKeyException {

		LOGGER.info("hashText(" + hashText + "), sharedSecret(" + password + "), algorithmName(" + algorithmName + ")");

		SecretKeySpec key = new SecretKeySpec((password).getBytes(StandardCharsets.UTF_8), algorithmName);
		Mac mac = Mac.getInstance(algorithmName);
		mac.init(key);

		byte[] bytes = mac.doFinal(hashText.getBytes(StandardCharsets.US_ASCII));

		StringBuilder hash = new StringBuilder();
		for (byte aByte : bytes) {
			String hex = Integer.toHexString(0xFF & aByte);
			if (hex.length() == 1) {
				hash.append('0');
			}
			hash.append(hex);
		}

		return hash.toString();
	}

}
