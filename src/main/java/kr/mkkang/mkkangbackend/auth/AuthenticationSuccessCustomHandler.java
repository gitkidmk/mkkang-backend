package kr.mkkang.mkkangbackend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mkkang.mkkangbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationSuccessCustomHandler implements AuthenticationSuccessHandler {

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

        log.debug("defaultOAuth2User = {}", defaultOAuth2User);
        log.debug("name = {}", name);
        log.debug("userEmail = {}", userEmail);
        log.debug("registrationId = {}", registrationId);


//        String tokenKey = userEmail + ":" + registrationId;
        String tokenKey = userEmail;

        // token 생성하기
        String accessToken = tokenService.generateAccessToken(tokenKey);
        String refreshToken = tokenService.generateRefreshToken(tokenKey);

        // token 저장하기
        tokenService.saveToken(accessToken, refreshToken);

        // token 전달하기
        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: HTTPS에서만 전송되도록 설정 : 추후 true로 변경
        cookie.setMaxAge(60*10);
        cookie.setPath("/");
        cookie.setDomain("localhost");

        log.debug("cookie.getName() = {}", cookie.getName());
        log.debug("cookie.getValue() = {}", cookie.getValue());

        response.addCookie(cookie);

        // 리다이렉트??
        response.sendRedirect("http://localhost:3000");

    }
}
