package com.pgcompliance.security;

import com.pgcompliance.entity.User;
import com.pgcompliance.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
    extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeader == null
        || !authorizationHeader.startsWith("Bearer ")) {

      filterChain.doFilter(request, response);
      return;
    }

    String token = authorizationHeader.substring(7);

    if (!jwtTokenProvider.validateToken(token)) {

      filterChain.doFilter(request, response);
      return;
    }

    String username = jwtTokenProvider.getUsernameFromToken(token);

    if (username != null
        && SecurityContextHolder
            .getContext()
            .getAuthentication() == null) {

      User user = userRepository
          .findByUsername(username)
          .orElse(null);

      if (user != null
          && Boolean.TRUE.equals(user.getActive())) {

        String authority = "ROLE_" + user.getRole().name();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user.getUsername(),
            null,
            List.of(
                new SimpleGrantedAuthority(
                    authority)));

        SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(
      HttpServletRequest request) {

    String path = request.getServletPath();

    return path.equals("/api/v1/auth/login");
  }
}