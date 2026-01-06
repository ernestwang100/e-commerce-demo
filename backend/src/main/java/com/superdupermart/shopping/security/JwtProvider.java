package com.superdupermart.shopping.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // resolves the token -> use the information in the token to create a userDetail object
    public Optional<AuthUserDetail> resolveToken(HttpServletRequest request){
        String prefixedToken = request.getHeader("Authorization"); // extract token value by key "Authorization"
        if (prefixedToken == null || !prefixedToken.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = prefixedToken.substring(7); // remove the prefix "Bearer "

        try {
            // Note: Using parserBuilder() for newer JJWT versions (refs likely used older parser())
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            // JWT claims return Integer for small numbers, need to convert to Long (or Integer for our app)
            // Our app uses Integer for ID, refs uses Long. Sticking to Integer as per our User entity
            Number userIdNum = (Number) claims.get("userId");
            Integer userId = userIdNum != null ? userIdNum.intValue() : null;
            
            List<LinkedHashMap<String, String>> permissions = (List<LinkedHashMap<String, String>>) claims.get("permissions");

            List<GrantedAuthority> authorities = permissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.get("authority")))
                    .collect(Collectors.toList());

            return Optional.of(new AuthUserDetail(
                    userId,
                    username,
                    null, // password not needed here
                    authorities
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String createToken(String username, String role, Integer userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId);
        
        // Match structure expected by resolveToken
        List<LinkedHashMap<String, String>> permissions = new java.util.ArrayList<>();
        LinkedHashMap<String, String> authority = new LinkedHashMap<>();
        authority.put("authority", role); // role is already "ROLE_ADMIN" or "ROLE_USER" in our system?
        // Wait, refs prepends ROLE_. Let's check AuthService. currently AuthService passes ROLE_ADMIN.
        permissions.add(authority);
        
        claims.put("permissions", permissions);
        // Also put role for backward compatibility if needed? No, resolveToken uses permissions.
        claims.put("role", role); 

        Date now = new Date();
        Date validity = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Kept for backward compatibility if other classes use it, but should migrate
    public String extractUsername(String token) {
         try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
