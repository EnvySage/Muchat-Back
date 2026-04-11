package com.xs.chat.Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JWTUtils {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String id){
        return JWT.create()
                .withClaim("id", id)
                .withExpiresAt(Instant.ofEpochSecond(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getIdFromToken(String token) {
        try {
            return JWT.decode(token).getClaim("id").asString();
        } catch (Exception e) {
            return null;
        }
    }


}
