package com.demo.dreamshops.security.jwt;

import com.demo.dreamshops.security.user.ShopUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.expirationInMils}")
    private int expirationTime;


    public String generateTokenForUser(Authentication authentication){

        Object principal = authentication.getPrincipal();

        String username;
        Long userId = null;
        List<String> roles;

        if (principal instanceof ShopUserDetails userPrincipal) {

            username = userPrincipal.getUsername();
            userId = userPrincipal.getId();

            roles = userPrincipal.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

        } else {
            UserDetails user = (UserDetails) principal;

            username = user.getUsername();

            roles = user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }

        return Jwts.builder()
                .subject(username)
                .claim("id", userId)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key())
                .compact();
    }


    private SecretKey key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String extractUsername(String token){
        return getClaims(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token){
        return getClaims(token).getExpiration();
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return getExpirationDateFromToken(token).before(new Date());
    }


    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (ExpiredJwtException |
                 UnsupportedJwtException |
                 MalformedJwtException |
                 SignatureException |
                 IllegalArgumentException e) {

            throw new JwtException("Invalid JWT: " + e.getMessage());
        }
    }

    public long getRemainingExpiration(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long now = System.currentTimeMillis();
        return expiration.getTime() - now;
    }
}