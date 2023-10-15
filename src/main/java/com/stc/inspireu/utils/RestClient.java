package com.stc.inspireu.utils;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class RestClient {

	private RestTemplate restTemplate;

	public RestClient() {
		this.restTemplate = new RestTemplate();
	}


	public JsonNode invoke(String httpMethodType, String path, HttpHeaders httpHeaders, JSONObject jSONObject) {

		if (httpMethodType != null && httpMethodType.equalsIgnoreCase("post")) {
			return post(path, httpHeaders, jSONObject);
		} else if (httpMethodType != null && httpMethodType.equalsIgnoreCase("put")) {
			return put(path, httpHeaders, jSONObject);
		} else if (httpMethodType != null && httpMethodType.equalsIgnoreCase("delete")) {
			return delete(path, httpHeaders);
		} else {
			return get(path, httpHeaders);
		}
	}

	public JsonNode get(String path, HttpHeaders httpHeaders) {

//        httpHeaders.add("Content-Type", "application/json");
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<String>("", httpHeaders);

		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});

		ResponseEntity<JsonNode> response = restTemplate.exchange(path, HttpMethod.GET, requestEntity, JsonNode.class);

		JsonNode jsonNode = response.getBody();

		return jsonNode;
	}

	public JsonNode post(String path, HttpHeaders httpHeaders, JSONObject jSONObject) {

//        httpHeaders.add("Content-Type", "application/json");
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<String>("", httpHeaders);

		if (jSONObject != null) {
			requestEntity = new HttpEntity<String>(jSONObject.toString(), httpHeaders);
		}

		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});

		ResponseEntity<JsonNode> response = restTemplate.exchange(path, HttpMethod.POST, requestEntity, JsonNode.class);

		JsonNode jsonNode = response.getBody();

		return jsonNode;
	}

	public JsonNode put(String path, HttpHeaders httpHeaders, JSONObject jSONObject) {

//        httpHeaders.add("Content-Type", "application/json");
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<String>("", httpHeaders);

		if (jSONObject != null) {
			requestEntity = new HttpEntity<String>(jSONObject.toString(), httpHeaders);
		}

		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});

		ResponseEntity<JsonNode> response = restTemplate.exchange(path, HttpMethod.PUT, requestEntity, JsonNode.class);

		JsonNode jsonNode = response.getBody();

		return jsonNode;
	}

	public JsonNode delete(String path, HttpHeaders httpHeaders) {

//        httpHeaders.add("Content-Type", "application/json");
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> requestEntity = new HttpEntity<String>("", httpHeaders);

		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});

		ResponseEntity<JsonNode> response = restTemplate.exchange(path, HttpMethod.DELETE, requestEntity,
				JsonNode.class);

		JsonNode jsonNode = response.getBody();

		return jsonNode;
	}

}
