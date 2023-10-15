package com.stc.inspireu.controllers;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.authn.SamlResponse;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.factory.SamlMessageFactory;
import com.onelogin.saml2.http.HttpRequest;
import com.onelogin.saml2.settings.Saml2Settings;
import com.stc.inspireu.dtos.VerifyAuthTokenDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.User;
import com.stc.inspireu.services.AuthService;
import com.stc.inspireu.services.UserService;
import com.stc.inspireu.utils.JwtUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.SamlSettings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RedirectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${samlPreserveOriginalUrl}")
    private String samlPreserveOriginalUrl;

    @Value("${cookie.domain}")
    private String cookieDomain;

    @Value("${ui.loginPath}")
    private String uiLoginPath;

    @Value("${ui.url}")
    private String uiUrl;

    @Value("${ui.samlSuccessPath}")
    private String uiSamlSuccessPath;

    @Value("${ui.ssoRedirect}")
    private String uiSsoRedirect;

    private final AuthService authService;

    private final SamlSettings samlSettings;

    private final JwtUtil jwtUtil;

    private final UserService userService;


    @GetMapping({"/app/samlSuccessRedirect"})
    public String appSmlSuccessRedirect() {
        return "samlRedirect";
    }

    @GetMapping("/saml/doLogin")
    public void doLogin(HttpServletRequest request, HttpServletResponse response) throws SettingsException, Error,
        IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        LOGGER.info("/saml/doLogin");

        Auth auth = new Auth((Saml2Settings) samlSettings.getSamlSettings().get("settings"), request, response);

        auth.login();
    }

    @GetMapping(value = "/saml/metadata")
    public void meta(HttpServletRequest request, HttpServletResponse response) throws SettingsException, Error,
        IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

        LOGGER.info("/saml/metadata");

        Auth auth = new Auth((Saml2Settings) samlSettings.getSamlSettings().get("settings"), request, response);

        Saml2Settings settings = auth.getSettings();

        settings.setSPValidationOnly(true);

        List<String> errors = settings.checkSettings();

        if (errors.isEmpty()) {
            String metadata = settings.getSPMetadata();
            InputStream in = new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));
            response.setContentType(MediaType.APPLICATION_XML_VALUE);
            StreamUtils.copy(in, response.getOutputStream());

        } else {
            for (String error : errors) {
                LOGGER.error(error);
            }
            InputStream in = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            StreamUtils.copy(in, response.getOutputStream());
        }

    }

    @PostMapping("/saml/SSO")
    public String sso(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOGGER.info("/saml/SSO");

        LOGGER.info(request.getRequestURI());
        LOGGER.info(request.getRequestURL().toString());

        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {

            String headerName = headerNames.nextElement();

            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                LOGGER.info(" " + headerValue);
            }

        }

        Auth auth = new Auth((Saml2Settings) samlSettings.getSamlSettings().get("settings"), request, response);

        auth.setSamlMessageFactory(new SamlMessageFactory() {
            @Override
            public SamlResponse createSamlResponse(Saml2Settings settings, HttpRequest request) throws Exception {
                SamlResponse sr = new SamlResponse(settings, request);
                if (samlPreserveOriginalUrl.equals("true")
                    && samlSettings.getSamlSettings().get("originalUrl") != null) {
                    sr.setDestinationUrl((String) samlSettings.getSamlSettings().get("originalUrl"));
                }
                return sr;
            }
        });

        auth.processResponse();

        if (auth.isAuthenticated()) {

            List<String> errors = auth.getErrors();

            if (errors.isEmpty()) {

                String nameId = auth.getNameId();
                String nameIdFormat = auth.getNameIdFormat();
                String sessionIndex = auth.getSessionIndex();
                String nameidNameQualifier = auth.getNameIdNameQualifier();
                String nameidSPNameQualifier = auth.getNameIdSPNameQualifier();

                try {
                    LOGGER.info("getSSOurl " + auth.getSSOurl());
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
                try {
                    LOGGER.info("getSLOurl " + auth.getSLOurl());
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
                try {
                    LOGGER.info("getSLOResponseUrl " + auth.getSLOResponseUrl());
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }

                LOGGER.info("nameId " + nameId);
                LOGGER.info("nameIdFormat " + nameIdFormat);
                LOGGER.info("sessionIndex " + sessionIndex);
                LOGGER.info("nameidNameQualifier " + nameidNameQualifier);
                LOGGER.info("nameidSPNameQualifier " + nameidSPNameQualifier);

                String tkn = authService.getAuthTokenSSO(nameId);

                if (cookieDomain.equals("") || cookieDomain.equals(null)) {

                    LOGGER.warn("cookie not set using query param for token redirect");

                    if (!tkn.equals("no_user_found")) {
                        LOGGER.info("saml success redirect");
                        return "redirect:" + uiUrl + uiSsoRedirect + "?authToken=" + tkn + "&assetToken="
                            + jwtUtil.generateAssetJwtToken(null, true);
                    } else {
                        LOGGER.error("user not registred");
                        LOGGER.error("saml response redirect to " + uiUrl + uiLoginPath
                            + "?ssoAuthFailure=user_not_registred&isSSO=true");
                        return "redirect:" + uiUrl + uiLoginPath + "?ssoAuthFailure=user_not_registred&isSSO=true";
                    }

                } else {
                    LOGGER.info("cookie set, it is using for token redirect");
                    if (!tkn.equals("no_user_found")) {

                        Cookie cookie = new Cookie("assetToken", jwtUtil.generateAssetJwtToken(null, true));
                        cookie.setMaxAge(120);
                        cookie.setPath("/sso-redirect");
                        cookie.setSecure(true);
                        cookie.setDomain(cookieDomain);
                        response.addCookie(cookie);

                        Cookie cookie1 = new Cookie("authToken", tkn);
                        cookie1.setMaxAge(120);
                        cookie1.setPath("/sso-redirect");
                        cookie1.setSecure(true);
                        cookie1.setDomain(cookieDomain);
                        response.addCookie(cookie1);

                        LOGGER.info("success redirect");
                        return "redirect:" + uiUrl + uiSsoRedirect;
                    } else {
                        LOGGER.error("user not registred");
                        LOGGER.error("saml response redirect to " + uiUrl + uiLoginPath
                            + "?ssoAuthFailure=user_not_registred&isSSO=true");
                        return "redirect:" + uiUrl + uiLoginPath + "?ssoAuthFailure=user_not_registred&isSSO=true";
                    }
                }

            } else {

                LOGGER.error(StringUtils.join(errors, ", "));

                if (auth.isDebugActive()) {
                    String errorReason = auth.getLastErrorReason();
                    if (errorReason != null && !errorReason.isEmpty()) {
                        LOGGER.error(auth.getLastErrorReason());
                    }
                }

                LOGGER.error(
                    "saml response redirect to " + uiUrl + uiLoginPath + "?ssoAuthFailure=saml_err&isSSO=true");
                return "redirect:" + uiUrl + uiLoginPath + "?ssoAuthFailure=saml_err&isSSO=true";
            }

        } else {
            LOGGER.error("user_not_authenticated rediecting to login page");
            LOGGER.error("saml response redirect to " + uiUrl + uiLoginPath
                + "?ssoAuthFailure=user_not_authenticated&isSSO=true");
            return "redirect:" + uiUrl + uiLoginPath + "?ssoAuthFailure=user_not_authenticated&isSSO=true";
        }

    }

    @ResponseBody
    @PostMapping("/saml/validate/ssoTokens")
    public ResponseEntity<?> validateSSOToken(HttpServletRequest request,
                                              HttpServletResponse response,
                                              @Valid @RequestBody VerifyAuthTokenDto verifyAuthTokenDto,
                                              BindingResult bindingResult) {

        Map<String, Object> claims = jwtUtil.getClaimsFromAuthToken(verifyAuthTokenDto.getAuthToken());

        if (claims != null) {
            User u = userService.findUserByEmailId((String) claims.get("email"));

            if (u != null && !u.getInvitationStatus().equals(Constant.BLOCKED.toString())) {

                Map<String, Object> d = new HashedMap<String, Object>();

                d.put("authToken", verifyAuthTokenDto.getAuthToken());

                eraseCookie(request, response);

                return ResponseWrapper.response(d);
            }

        }

        return ResponseWrapper.response(null, HttpStatus.UNAUTHORIZED);
    }

    private void eraseCookie(HttpServletRequest req, HttpServletResponse resp) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                resp.addCookie(cookie);
            }
    }

}
