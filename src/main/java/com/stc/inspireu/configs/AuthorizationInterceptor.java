package com.stc.inspireu.configs;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.beans.CurrentPermissionObject;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.models.User;
import com.stc.inspireu.services.ResourcePermissionService;
import com.stc.inspireu.services.UserService;
import com.stc.inspireu.utils.AuthorizeUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import com.stc.inspireu.utils.Utility;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private Utility utility;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourcePermissionService resourcePermissionService;

    private String urlPrefix = "/api/v1";

    private static final String[] AUTH_WHITELIST = {"/api/v1/auth/*", "/api/v1/startups/registration",
        "/api/v1/management/registration", "/api/v1/publicForms/**", "/api/v1/sse/**",
        "/api/v1/formTemplates/basic", "/api/v1/initData", "/app/samlSuccessRedirect", "/api/v1/status",
        "/test/**", "/api/v1/publicForms/formTemplates/**", "/api/v1/management/files/settings/**",
        "/api/v1/publicForms/intakePrograms/_registrations/**","/api/v1/publicForms/countries/**"};

    @SuppressWarnings("unchecked")
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                             Object handler) throws Exception {

        String requestURI = httpServletRequest.getRequestURI();

        String method = httpServletRequest.getMethod();

        final Map<String, String> pathVariables = (Map<String, String>) httpServletRequest
            .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");

        CurrentPermissionObject currentPermissionObject = new CurrentPermissionObject();

        httpServletRequest.setAttribute("currentPermissionObject", currentPermissionObject);

        long userId = 0L;

        if (currentUserObject != null && currentUserObject.getUserId() != null) {
            userId = (long) currentUserObject.getUserId();
        }

        if (utility.isUrlMatch(requestURI, AUTH_WHITELIST)) {
            return grantAccess(httpServletRequest, httpServletResponse);
        }

        if (handler instanceof HandlerMethod) {

            HandlerMethod handlerMethod = (HandlerMethod) handler;

            Authorize authorizeAnnotation = handlerMethod.getMethodAnnotation(Authorize.class);

            if (null != authorizeAnnotation) {

                String[] roles = authorizeAnnotation.roles();

                String permission = authorizeAnnotation.permission();

                String resource = authorizeAnnotation.resource();

                String resourceId = authorizeAnnotation.resourceId();

                boolean checkRoleUrlScope = authorizeAnnotation.checkRoleUrlScope();

                String logic = authorizeAnnotation.logic();

                String parentId = authorizeAnnotation.parentId();

                String parentResource = authorizeAnnotation.parentResource();

                String grandParentId = authorizeAnnotation.grandParentId();

                String grandParentResource = authorizeAnnotation.grandParentResource();

                User currentUser = userService.getUserRoles(userId);

                if (currentUser != null) {

                    CurrentPermissionObject cp1 = new CurrentPermissionObject(currentUser.getRole().getId(),
                        currentUser.getRole().getRoleName());

                    httpServletRequest.setAttribute("currentPermissionObject", cp1);

                    if (currentUser.getRole().getRoleName().equals(RoleName.ROLE_SUPER_ADMIN)) { // super admin has all
                        // previlages

                        return grantAccess(httpServletRequest, httpServletResponse);
                    }

                } else {
                    return denyAccess(httpServletRequest, httpServletResponse);
                }

                if (logic.equals(AuthorizeUtil.roleOrPermission)) {

                    if (roles.length > 0 || !permission.equals("")) {

                        boolean isRole = Arrays.asList(roles).contains(currentUser.getRole().getRoleName());

                        if (isRole) {
                            return grantAccess(httpServletRequest, httpServletResponse);
                        } else {
                            Long rId = 0L, pId = 0L, gpId = 0L;

                            try {
                                rId = Long.parseLong(pathVariables.get(resourceId));
                            } catch (Exception e) {
                            }

                            try {
                                pId = Long.parseLong(pathVariables.get(parentId));
                            } catch (Exception e) {
                            }

                            try {
                                gpId = Long.parseLong(pathVariables.get(grandParentId));
                            } catch (Exception e) {
                            }
                            boolean isAllow = resourcePermissionService.isPermit(userId, resource, parentResource,
                                grandParentResource, rId, pId, gpId, permission);

                            if (isAllow) {
                                return grantAccess(httpServletRequest, httpServletResponse);
                            }
                            return denyAccess(httpServletRequest, httpServletResponse);
                        }

                    } else if (checkRoleUrlScope) {
                        if (utility.isUrlMatch(requestURI, urlPrefix + currentUser.getRole().getUrlScope())) {
                            return grantAccess(httpServletRequest, httpServletResponse);
                        }
                        return denyAccess(httpServletRequest, httpServletResponse);
                    }

                    return denyAccess(httpServletRequest, httpServletResponse);

                }

                if (roles.length > 0 && !permission.equals("")) {

                    boolean isRole = Arrays.asList(roles).contains(currentUser.getRole().getRoleName());

                    if (isRole) {

                        Long rId = 0L, pId = 0L, gpId = 0L;

                        try {
                            rId = Long.parseLong(pathVariables.get(resourceId));
                        } catch (Exception e) {
                        }

                        try {
                            pId = Long.parseLong(pathVariables.get(parentId));
                        } catch (Exception e) {
                        }

                        try {
                            gpId = Long.parseLong(pathVariables.get(grandParentId));
                        } catch (Exception e) {
                        }

                        boolean isAllow = resourcePermissionService.isPermit(userId, resource, parentResource,
                            grandParentResource, rId, pId, gpId, permission);

                        if (isAllow) {
                            return grantAccess(httpServletRequest, httpServletResponse);
                        }

                        return denyAccess(httpServletRequest, httpServletResponse);

                    }

                    return denyAccess(httpServletRequest, httpServletResponse);

                } else if (checkRoleUrlScope) {

                    if (utility.isUrlMatch(requestURI, urlPrefix + currentUser.getRole().getUrlScope())) {

                        return grantAccess(httpServletRequest, httpServletResponse);

                    }

                    return denyAccess(httpServletRequest, httpServletResponse);

                }

                return denyAccess(httpServletRequest, httpServletResponse);

            }

            return grantAccess(httpServletRequest, httpServletResponse);

        }

        return notFound(httpServletRequest, httpServletResponse);
    }

    boolean denyAccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws JsonGenerationException, JsonMappingException, IOException {
        LOGGER.info("AuthorizationInterceptor access denied " + httpServletRequest.getMethod() + " "
            + utility.getFullURL(httpServletRequest));
        ResponseWrapper.createResponse403("access denied", "user|authToken", httpServletResponse);
        return false;
    }

    boolean grantAccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return true;
    }

    boolean notFound(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        LOGGER.info("AuthorizationInterceptor HandlerMethod not found " + httpServletRequest.getMethod() + " "
            + utility.getFullURL(httpServletRequest));
        ResponseWrapper.createResponse404(httpServletResponse);
        return false;
    }

}
