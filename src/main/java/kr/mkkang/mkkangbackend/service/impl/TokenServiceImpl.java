package kr.mkkang.mkkangbackend.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kr.mkkang.mkkangbackend.redis.Token;
import kr.mkkang.mkkangbackend.redis.TokenRepository;
import kr.mkkang.mkkangbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

// interface를 두는 것이 맞는가 abstract class? 를 두는 것이 맞는가...

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Value("${jwt.accesstoken.secretkey}")
    private String accessSecretKey;

    @Value("${jwt.refreshtoken.secretkey}")
    private String refreshSecretKey;

    @Value("${jwt.accesstoken.expiration}")
    private long aceessTokenExpiration;

    @Value("${jwt.accesstoken.expiration}")
    private long refreshTokenExpriation;

    private static SecretKey ACCESS_SECRET_KEY;
    private static SecretKey REFRESH_SECRET_KEY;
    private static long ACCESSTOKEN_EXPIRATION;
    private static long REFRESHTOKEN_EXPRIATION;

    private static final AeadAlgorithm ALGORITHM = Jwts.ENC.A128CBC_HS256;

    @PostConstruct
    public void init() {
        ACCESS_SECRET_KEY = Keys.hmacShaKeyFor(Base64.getEncoder().encodeToString(this.accessSecretKey.getBytes()).getBytes());
        REFRESH_SECRET_KEY = Keys.hmacShaKeyFor(Base64.getEncoder().encodeToString(this.refreshSecretKey.getBytes()).getBytes());
//        ACCESS_SECRET_KEY = Jwts.ENC.A128CBC_HS256.key().build();
//        REFRESH_SECRET_KEY = Jwts.ENC.A128CBC_HS256.key().build();
        ACCESSTOKEN_EXPIRATION = this.aceessTokenExpiration;
        REFRESHTOKEN_EXPRIATION = this.refreshTokenExpriation;
    }


    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    public static final String VALIDATION_FAILED = "this token is not available";

    public void saveToken(String accessToken, String refreshToken) {
        try {
            tokenRepository.save(new Token(accessToken, refreshToken));
        } catch (DataAccessException e) {
            log.error("error 1 = {}", e.toString());
        }
    }
    public void removeToken(String accessToken) {
        try {
            tokenRepository.deleteById(accessToken);
        } catch (DataAccessException e) {
            log.error("error 2 = {}", e.getMessage());
        }
    }

    public String getSubjectFromToken(String tokenType, String token) {
        return getClaimFromToken(tokenType, token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String tokenType, String token) {
        return getClaimFromToken(tokenType, token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String tokenType, String token, Function<Claims, T> claimsResolver) {
        SecretKey tokenKey = ACCESS_TOKEN.equals(tokenType) ? ACCESS_SECRET_KEY : REFRESH_SECRET_KEY;
        Claims claims = getAllClaimsFromToken(token, tokenKey);
        log.debug("claims = {}", claims);
        return claimsResolver.apply(claims);
    }

    // JWT 토큰에서 모든 클레임 추출
    private Claims getAllClaimsFromToken(String token, SecretKey tokenKey) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .verifyWith(tokenKey)
//                    .decryptWith(tokenKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch(Exception e) {
            log.error("error 3", e.getMessage());

        }
        return claims;
    }

    // JWT 토큰이 만료되었는지 확인
    private Boolean isTokenExpirationPass(String tokenType, String token) {
        final Date expiration = getExpirationDateFromToken(tokenType, token);
        return expiration.after(new Date());
    }

    public String generateAccessToken(String userName) {
        return generateToken(ACCESS_TOKEN, userName);
    }

    public String generateRefreshToken(String token) {
        return generateToken(REFRESH_TOKEN, token);
    }

    // JWT 토큰 생성
    public String generateToken(String tokenType, String username) {
        return Jwts.builder()
                .header()
                .add("alg", "A128CBC_HS256")
                .and()
                .subject(username) // subject와 claim 차이?
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() +
                        (ACCESS_TOKEN.equals(tokenType) ?
                        ACCESSTOKEN_EXPIRATION : REFRESHTOKEN_EXPRIATION) * 1000))
                .signWith(ACCESS_TOKEN.equals(tokenType) ?
                        ACCESS_SECRET_KEY : REFRESH_SECRET_KEY)
//                .encryptWith(ACCESS_TOKEN.equals(tokenType) ?
//                        ACCESS_SECRET_KEY : REFRESH_SECRET_KEY, ALGORITHM)
                .compact();
    }

    // JWT token validation
    public String validateAccessToken(String accessToken) {
        String result = VALIDATION_FAILED;

        if(getSubjectFromToken(ACCESS_TOKEN, accessToken) != null && !accessToken.isEmpty()) {
            if(isTokenExpirationPass(ACCESS_TOKEN, accessToken)) {
                result = accessToken;
            } else {
                // refresh token 검사
                String refreshToken = tokenRepository.findById(accessToken).get().getRefreshToken();
                if(getSubjectFromToken(REFRESH_TOKEN, refreshToken) != null && isTokenExpirationPass(REFRESH_TOKEN, refreshToken)) {
                    // accessToken 재생성 및 redis 저장
                    String userInfo = getSubjectFromToken(ACCESS_TOKEN, accessToken);
                    String newAccessToken = this.generateToken(ACCESS_TOKEN, userInfo);
                    tokenRepository.save(new Token(newAccessToken, refreshToken));
                    result = newAccessToken;
                }
            }
        } else {
            throw new JwtException(VALIDATION_FAILED);
        }

        return result;
    }

}