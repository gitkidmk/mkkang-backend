package kr.mkkang.mkkangbackend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mkkang.mkkangbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationSuccessCustomHandler implements AuthenticationSuccessHandler {

    @Value("${client.ip}")
    private String domain;

    @Value("${client.port}")
    private int port;

    @Value("${client.protocol}")
    private String protocol;

    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // authentication을 통해 사용자 정보 저장하는데 이를 불러와야 함
        // 이메일, registration_id 와 같은 정보
        // TODO: 여기서부터!!!
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        defaultOAuth2User.getAttribute("email");
        String name = defaultOAuth2User.getName();

        String userEmail = defaultOAuth2User.getAttribute("email");
        String registrationId = defaultOAuth2User.getAttribute("registrationId");
        String role = defaultOAuth2User.getAuthorities().toString();

        log.debug("defaultOAuth2User = {}", defaultOAuth2User);
        log.debug("name = {}", name);
        log.debug("userEmail = {}", userEmail);
        log.debug("registrationId = {}", registrationId);
        log.debug("role = {}", role);


//        String tokenKey = userEmail + ":" + registrationId;
        String tokenKey = userEmail;

        // token 생성하기
        String accessToken = tokenService.generateAccessToken(tokenKey);
        String refreshToken = tokenService.generateRefreshToken(tokenKey);

        // token 저장하기
        tokenService.saveToken(accessToken, refreshToken);
        // token 전달하기
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .domain("." + domain)
                .maxAge(60*10)
                .build();

        ResponseCookie roleCookie = ResponseCookie.from("role", role)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .domain("." + domain)
                .maxAge(60*10)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", roleCookie.toString());

        // 리다이렉트??
        String url = "%s://%s:%s/main".formatted(protocol, domain, port);
        response.sendRedirect(url);
        log.info("url = {}", url);

    }
}
