package com.stc.inspireu.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseWrapper {

	private static Map<String, Object> errorObj(Object obj) {
		if (obj == null) {
			return null;
		} else {
			Map<String, Object> data = new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					put("cause", obj);
				}
			};

			return data;
		}
	}

	public static ResponseEntity<Object> response(String str, HttpStatus status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", null);

		if (status.value() > 399) {
			map.put("error", errorObj(str));
			map.put("message", str);
		} else {
			map.put("message", str);
			map.put("error", null);
		}

		return new ResponseEntity<Object>(map, status);
	}

	public static ResponseEntity<Object> response(String message, String error, HttpStatus status) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", null);
		map.put("message", message);
		map.put("error", errorObj(error));
		return new ResponseEntity<Object>(map, status);
	}

	public static ResponseEntity<Object> response400(String message, String error) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", null);
		map.put("message", message);
		map.put("error", errorObj(error));
		return new ResponseEntity<Object>(map, HttpStatus.BAD_REQUEST);
	}

	public static ResponseEntity<Object> response401(String message, String error) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", null);
		map.put("message", message);
		map.put("error", errorObj(error));
		return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
	}

	public static ResponseEntity<Object> response(Object data, String message) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", data);
		map.put("message", message);
		map.put("error", null);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public static ResponseEntity<Object> response(Object data) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", data);
		map.put("message", null);
		map.put("error", null);
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	public static void createResponse401(String message, String error, HttpServletResponse response)
			throws JsonGenerationException, JsonMappingException, IOException {

		Map<String, Object> details = new HashMap<>();
		details.put("data", null);
		details.put("message", message);
		details.put("error", errorObj(error));

		ObjectMapper mapper = new ObjectMapper();
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		mapper.writeValue(response.getWriter(), details);
	}

	public static void createResponse403(String message, String error, HttpServletResponse response)
			throws JsonGenerationException, JsonMappingException, IOException {

		Map<String, Object> details = new HashMap<>();
		details.put("data", null);
		details.put("message", message);
		details.put("error", errorObj(error));

		ObjectMapper mapper = new ObjectMapper();
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		mapper.writeValue(response.getWriter(), details);
	}

	public static ResponseEntity<Object> createResponseFromValidationErrors(List<ObjectError> allErrors,
			HttpStatus status) {

		ObjectError objectError = allErrors.get(0);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", null);
		map.put("message", objectError.getDefaultMessage());
		map.put("error", errorObj(((FieldError) objectError).getField()));
		return new ResponseEntity<Object>(map, status);
	}

	public static ResponseEntity<Object> create400ResponseFromValidationErrors(List<ObjectError> allErrors) {

		ObjectError objectError = allErrors.get(0);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", null);
		map.put("message", objectError.getDefaultMessage());
		map.put("error", errorObj(((FieldError) objectError).getField()));
		return new ResponseEntity<Object>(map, HttpStatus.BAD_REQUEST);
	}

	public static void createResponse404(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	public static void create400Response(HttpServletResponse response, String message, String error)
			throws JsonGenerationException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		Map<String, Object> details = new HashMap<>();
		details.put("data", null);
		details.put("message", message);
		details.put("error", errorObj(error));

		ObjectMapper mapper = new ObjectMapper();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		mapper.writeValue(response.getWriter(), details);
	}

}
