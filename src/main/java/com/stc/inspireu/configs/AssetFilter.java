package com.stc.inspireu.configs;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;

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
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;

@Component
public class AssetFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private Utility utility;

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException {
		String assetToken = httpServletRequest.getParameter("assetToken");

		if (assetToken != null) {
			Map<String, Object> claims = jwtUtil.getClaimsFromAssetToken(assetToken);

			if (claims != null) {
				filterChain.doFilter(httpServletRequest, httpServletResponse);
			} else {
				LOGGER.error("assetToken invalid");
				ResponseWrapper.createResponse404(httpServletResponse);
			}

		} else {
			LOGGER.error("assetToken invalid");
			ResponseWrapper.createResponse404(httpServletResponse);
		}

	}
}
