package kr.mkkang.mkkangbackend.service;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface TokenService {
    void saveToken(String accessToken, String refreshToken);

    void removeToken(String accessToken);

    String getSubjectFromToken(String tokenType, String token);

    Date getExpirationDateFromToken(String tokenType, String token);

    <T> T getClaimFromToken(String tokenType, String token, Function<Claims, T> claimsResolver);

    String generateAccessToken(String token);

    String generateRefreshToken(String token);

    String generateToken(String tokenType, String username);

    String validateAccessToken(String accessToken);
}
