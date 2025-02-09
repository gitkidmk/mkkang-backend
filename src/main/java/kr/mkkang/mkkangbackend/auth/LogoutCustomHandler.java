package kr.mkkang.mkkangbackend.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mkkang.mkkangbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class LogoutCustomHandler implements LogoutSuccessHandler {

    @Value("${client.port}")
    private int port;

    @Value("${client.ip}")
    private String domain;

    @Value("${client.protocol}")
    private String protocol;

    private final TokenService tokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 목표 : redis에서 사용자 token 지우기
        // TODO: 아래 코드 최신 문법으로 변경!
        String accessToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if("role".equals(cookie.getName())) {
                    ResponseCookie roleCookie = ResponseCookie.from("role", cookie.getValue())
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .domain("." + domain)
                            .maxAge(0)
                            .build();
                    response.addHeader("Set-Cookie", roleCookie.toString());
                }
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    tokenService.removeToken(accessToken);
                    ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .domain("." + domain)
                            .maxAge(0)
                            .build();

                    response.addHeader("Set-Cookie", accessCookie.toString());

                }
            }
        }

        // 토큰 지우기
        // TODO: null 체크 필요한가?
        // response에 성공 여부 설정하기
        // logout에서 default response가 궁금하군...
        // 리다이렉트??
        try {
            String url = "%s://%s:%s".formatted(protocol, domain, port);
            response.sendRedirect(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
