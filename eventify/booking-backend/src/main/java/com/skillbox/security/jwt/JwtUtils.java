package com.skillbox.security.jwt;

import com.skillbox.security.AppUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + Duration.of(7, ChronoUnit.DAYS).toMillis()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validate(String token){
        try{
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch (SignatureException e){
            log.error("Неправильная подпись: {}",e.getMessage());
        }catch (MalformedJwtException e){
            log.error("Неправильный токен: {}", e.getMessage());
        }catch (ExpiredJwtException e){
            log.error("Токен просрочен: {}",e.getMessage());
        }catch (UnsupportedJwtException e){
            log.error("Токен не поддерживается: {}",e.getMessage());
        }catch (IllegalArgumentException e){
            log.error("Claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
