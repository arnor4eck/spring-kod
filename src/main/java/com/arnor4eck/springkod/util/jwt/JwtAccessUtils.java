package com.arnor4eck.springkod.util.jwt;

import com.arnor4eck.springkod.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAccessUtils implements JwtUtils<User>{
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.lifetime}")
    private long lifetime;

    @Override
    public String generateToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        Date issued = new Date();
        Date expired = new Date(issued.getTime() + lifetime);
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims) // полезная информация
                .subject(user.getEmail())
                .issuedAt(issued) // создание
                .expiration(expired) // истечение
                .signWith(key) // секрет
                .compact();
    }

    public boolean validate(String token){
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token){
        return getClaimsFromToken(token).getSubject();
    }

    @SuppressWarnings("unchecked") // generateToken - поле roles предсталено в виде списка строк, приведение корректно
    public List<String> getRoles(String token){
        return getClaimsFromToken(token).get("roles", List.class);
    }

    private Claims getClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

