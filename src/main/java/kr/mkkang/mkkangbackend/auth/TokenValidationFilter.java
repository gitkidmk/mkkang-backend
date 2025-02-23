package kr.mkkang.mkkangbackend.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mkkang.mkkangbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenValidationFilter implements Filter {
    private final TokenService tokenService;

    @Value("${client.ip}")
    private String domain;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 1. 사용자로부터 accessToken rkwudhrl
        String accessToken = null;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.debug("cookie.getName() = {}", cookie.getName());
                log.debug("cookie.getValue() = {}", cookie.getValue());
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                }
            }
        }

        // 2. validateToken에 넘긴다
        String requestURL = request.getRequestURL().toString();
        log.debug("requestURL = {}", requestURL);

        if(!requestURL.contains("/login") && !requestURL.contains("/oauth2") && !requestURL.contains("/error") && !requestURL.contains("/logout")) {

            if(accessToken == null || accessToken.isEmpty()) {
                log.error("accesstoken = {}", accessToken);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                throw new ServletException("Invalid access token");
            } else {
                accessToken = tokenService.validateAccessToken(accessToken);

                // 3. response에 담는다

                // token 전달하기
                ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .domain("." + domain)
                        .maxAge(60*10)
                        .build();
                response.addHeader("Set-Cookie", cookie.toString());


                filterChain.doFilter(request, response);
            }
        }

        // 아래 코드 작성으로 해결 + G...BeanFilter가 아닌 그냥 Filter로 변경 + Bean으로 생성 아닌 그냥 new 로 객체 생성
        // https://docs.spring.io/spring-security/reference/servlet/architecture.html#adding-custom-filter
        // Be careful when you declare your filter as a Spring bean, either by annotating it with @Component or by declaring it as a bean in your configuration, because Spring Boot will automatically register it with the embedded container. That may cause the filter to be invoked twice, once by the container and once by Spring Security and in a different order.
    }
}
