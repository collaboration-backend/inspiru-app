package com.stc.inspireu.configs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.Utility;

@Component
public class SwaggerCustomFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private Utility utility;

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException {

		if (httpServletRequest.getServletPath().equals("/swagger-ui")) {
			httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/swagger-ui/");
			return;
		}

		if (httpServletRequest.getServletPath().contains("/swagger-ui/swagger-ui.css")
				&& !httpServletRequest.getServletPath().contains("/swagger-ui/swagger-ui.css.map")) {
			LOGGER.info("SwaggerCssFilter: " + utility.getFullURL(httpServletRequest));

			String orig = toText(
					getClass().getResourceAsStream("/META-INF/resources/webjars/springfox-swagger-ui/swagger-ui.css"));

			String _css = ".wrapper {\n" + "    max-width: 100% !important;\n" + "}";

			String css = orig + _css;

			httpServletResponse.setCharacterEncoding("UTF-8");
			httpServletResponse.setContentType("text/css;charset=UTF-8");
			httpServletResponse.setStatus(HttpServletResponse.SC_OK);
			httpServletResponse.getWriter().write(css);
		} else {
			filterChain.doFilter(httpServletRequest, httpServletResponse);
		}

	}

	String toText(InputStream in) {
		return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines()
				.collect(Collectors.joining("\n"));
	}

}
