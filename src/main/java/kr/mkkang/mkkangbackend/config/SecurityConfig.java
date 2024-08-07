package kr.mkkang.mkkangbackend.config;

import kr.mkkang.mkkangbackend.auth.AuthenticationSuccessCustomHandler;
import kr.mkkang.mkkangbackend.auth.OAuth2UserCustomService;
import kr.mkkang.mkkangbackend.auth.LogoutCustomHandler;
import kr.mkkang.mkkangbackend.auth.TokenValidationFilter;
import kr.mkkang.mkkangbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final OAuth2UserCustomService oAuth2UserService;
    private final TokenService tokenService;
    private final LogoutCustomHandler logoutHandler;
    private final AuthenticationSuccessCustomHandler authenticationSuccessHandler;

    private final TokenValidationFilter tokenValidationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // oauth2login해서 사용자 정보 DB 에 저장
        // token 발급해 redis에 저장

        // HttpSecurity 에 다양한 메소드 존재
        // 그중에서도 addFilter, authorizeHttpRequest, cors, csrf, oauth2Login, logout
        return http
                // cors
                .cors(Customizer.withDefaults()) // 이거 지워도 cross origin resource shared 막아줘야함
                // csrf
//                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(csrf -> csrf.disable())
                // authorizeHttpRequests : login 요청은 permitAll, 그 외의 요청은 인증검사
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/logout/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/family/**").hasAnyRole("FAMILY", "ADMIN")
                        .requestMatchers("/**").hasAnyRole("ANY", "FAMILY", "ADMIN"))
                // addFilterBefore : token validation filter가 최상단에 배치해 아래 filter까지 가서 spring이 고생하지 않도록
                .addFilterBefore(new TokenValidationFilter(tokenService), AuthorizationFilter.class)
                // oauth2Login
                .oauth2Login(login -> login
                        .userInfoEndpoint(info -> info
                                // userService : 사용자 정보 저장
                                .userService(oAuth2UserService))
                        // successHandler에서 + redis token 생성 및 저장 및 전달
                        .successHandler(authenticationSuccessHandler)
                        // failureHandler 고민하기 : default 값을 활용하는 것도 방법이겠다
                        )
                // logout : 사용자에게 token 지워주기 + redis에서도 지우기, 기본적으로 JSESSION_ID 쿠키값을 지운다고 함
                .logout(logout -> logout
                        .permitAll()
//                        .deleteCookies("access_token", "JSESSION_ID")
                        .logoutSuccessHandler(logoutHandler))
                .build();
    }

    @Bean
    public FilterRegistrationBean<TokenValidationFilter> tenantFilterRegistration(TokenValidationFilter filter) {
        FilterRegistrationBean<TokenValidationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 허용할 도메인 설정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

