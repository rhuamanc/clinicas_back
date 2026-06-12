package com.magm.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j

public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();

        // Parámetros
        Map<String, String[]> paramMap = request.getParameterMap();
        StringBuilder params = new StringBuilder();
        if (!paramMap.isEmpty()) {
            params.append(" [params: ");
            paramMap.forEach((k, v) -> params.append(k).append("=").append(Arrays.toString(v)).append("; "));
            params.append("]");
        }

        // Headers clave
        String auth = request.getHeader("Authorization");
        String origin = request.getHeader("Origin");
        String userAgent = request.getHeader("User-Agent");

        // Usuario desde JWT
        String usuario = null;
        if (auth != null && auth.startsWith("Bearer ") && jwtSecret != null && !jwtSecret.isBlank()) {
            try {
                String token = auth.substring(7);
                Claims claims = Jwts.parser()
                        .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                usuario = claims.getSubject();
            } catch (Exception e) {
                usuario = "INVALID_JWT";
            }
        }

        log.info("[HTTP IN] {} {}{}{} [Origin: {}] [User-Agent: {}] [Usuario: {}] [Auth: {}]", 
                method, path, query == null ? "" : "?" + query, params, origin, userAgent, usuario, auth != null ? "present" : "none");

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            // Headers de respuesta clave
            String expose = response.getHeader("Access-Control-Expose-Headers");
            String notif = response.getHeader("X-Notification");
            log.info("[HTTP OUT] {} {} -> {} ({} ms) [Expose-Headers: {}] [X-Notification: {}]", 
                    method, path, response.getStatus(), durationMs, expose, notif);
        }
    }
}
