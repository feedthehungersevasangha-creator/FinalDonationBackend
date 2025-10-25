//package com.komal.template_backend.Util;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.springframework.stereotype.Component;
//import io.jsonwebtoken.security.Keys;
//import java.security.Key;
//import java.util.Date;
//
//@Component
//public class JwtUtil
//{
//    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // generated dynamically
//    private final long EXPIRATION_TIME = 86400000; // 1 day
//
//    public String generateToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(secretKey)
//                .compact();
//    }
//
//    public String extractEmail(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//}
