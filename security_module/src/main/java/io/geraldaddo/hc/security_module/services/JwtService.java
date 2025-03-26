package io.geraldaddo.hc.security_module.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.expiration-time}")
    private long jwtExpiration;

    public List<SimpleGrantedAuthority> extractRoles(String token) {
        Claims claims = getAllClaims(token);
        List<?> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
    public Integer getUserId(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("userId", Integer.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .decryptWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
