package com.authentication.api.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {
    @Value("${auth.signing.key}")
    private String secret;

    @Value("${auth.expires-in}")
    private Duration expiresIn;

    public String generateToken() {
        long millis = expiresIn.toMillis();
        return JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + millis))
                .sign(Algorithm.HMAC256(secret));
    }
}
