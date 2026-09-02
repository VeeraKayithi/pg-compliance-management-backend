package com.pgcompliance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

  private static final String SECRET = "pgcompliancejwtsecretkeypgcompliancejwtsecretkey";

  private final Key key = Keys.hmacShaKeyFor(
      SECRET.getBytes());

  public String generateToken(
      String username) {

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(
                System.currentTimeMillis()
                    + 86400000))
        .signWith(key)
        .compact();
  }
}