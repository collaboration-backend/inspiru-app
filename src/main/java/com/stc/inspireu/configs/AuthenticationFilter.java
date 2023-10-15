package com.stc.inspireu.configs;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Utility utility;

    private static final String[] AUTH_WHITELIST = {"/api/v1/auth/*", "/api/v1/startups/registration",
        "/api/v1/management/registration", "/api/v1/formTemplates/basic", "/api/v1/initData",
        "/app/samlSuccessRedirect", "/api/v1/status", "/test/**", "/api/v1/general/**",
        "/api/v1/publicForms/formTemplates/**", "/api/v1/management/files/settings/**",
        "/api/v1/publicForms/intakePrograms/_registrations/**", "/api/v1/publicForms/countries/**",
        "/api/v1/contact-us/subjects", "/api/v1/contact-us/submit", "/api/v1/beneficiary/signup", "/api/v1/beneficiary/otp/**"};

    private static final String[] AUTH_WHITELIST_PUBLIC_ACCESS_FORM_TOKEN = {"/api/v1/publicForms/**",
        "/api/v1/management/files/settings", "/api/v1/assetToken"};

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = httpServletRequest.getHeader("Authorization");

        String requestURI = httpServletRequest.getRequestURI();

        if (utility.isUrlMatch(requestURI, AUTH_WHITELIST)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        if (utility.isUrlMatch(requestURI, AUTH_WHITELIST_PUBLIC_ACCESS_FORM_TOKEN)) {

            String formToken = httpServletRequest.getParameter("formToken");

            Map<String, Object> claims = Objects.nonNull(formToken) && !formToken.isEmpty() ? jwtUtil.getClaimsFromGenericToken(formToken) : null;

            if (claims != null) {

                httpServletRequest.setAttribute("currentUserObject", getCurrentUserObject(claims));
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

        }

        if (authHeader != null && authHeader.length() > 10) {

            String authToken = authHeader.substring(7);

            Map<String, Object> claims = jwtUtil.getClaimsFromAuthToken(authToken);

            if (claims != null) {

                httpServletRequest.setAttribute("currentUserObject", getCurrentUserObject(claims));
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }

            LOGGER.error(
                "AuthenticationFilter invalid AuthHeader requestURI: " + utility.getFullURL(httpServletRequest));

            if (utility.isUrlMatch(requestURI, AUTH_WHITELIST_PUBLIC_ACCESS_FORM_TOKEN)) {
                ResponseWrapper.createResponse401("invalid authToken or formToken", "authToken|formToken",
                    httpServletResponse);
                return;
            } else {
                ResponseWrapper.createResponse401("invalid authToken", "authToken", httpServletResponse);
                return;
            }

        }

        LOGGER.error("AuthenticationFilter invalid AuthHeader requestURI: " + utility.getFullURL(httpServletRequest));

        if (utility.isUrlMatch(requestURI, AUTH_WHITELIST_PUBLIC_ACCESS_FORM_TOKEN)) {
            ResponseWrapper.createResponse401("invalid authToken or formToken", "authToken|formToken",
                httpServletResponse);
            return;
        } else {
            ResponseWrapper.createResponse401("invalid authToken", "authToken", httpServletResponse);
            return;
        }
    }

    CurrentUserObject getCurrentUserObject(Map<String, Object> _claims) {

        Map<String, Object> claims = _claims;

        CurrentUserObject currentUserObject = new CurrentUserObject();

        String email = "";

        try {
            email = claims.get("email").toString();
        } catch (Exception e) {
            LOGGER.error("invalid userId");
        }

        long userId = 0L;

        try {
            userId = ((Number) claims.get("userId")).longValue();
        } catch (Exception e) {
            LOGGER.error("invalid userId");
        }

        currentUserObject.setEmail(email);
        currentUserObject.setUserId(userId);

        claims.remove("email");
        claims.remove("userId");

        Map<String, Object> metaData = new HashMap<String, Object>();

        for (String key : claims.keySet()) {
            metaData.put(key, claims.get(key));
        }

        currentUserObject.setMetaData(metaData);

        return currentUserObject;
    }

}
