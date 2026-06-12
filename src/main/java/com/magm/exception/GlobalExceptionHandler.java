package com.magm.exception;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${jwt.secret}")
    private String jwtSecret;


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        logErrorEnriquecido(req, ex, 401, "Credenciales inválidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, "Credenciales inválidas", System.currentTimeMillis()));
    }


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex, HttpServletRequest req) {
        logErrorEnriquecido(req, ex, 403, "Cuenta deshabilitada. Contacte al administrador.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(403, "Cuenta deshabilitada. Contacte al administrador.", System.currentTimeMillis()));
    }


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleExpiredJwt(ExpiredJwtException ex, HttpServletRequest req) {
        logErrorEnriquecido(req, ex, 401, "Token expirado. Inicia sesion nuevamente.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(401, "Token expirado. Inicia sesion nuevamente.", System.currentTimeMillis()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        logErrorEnriquecido(req, ex, 400, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(400, message, System.currentTimeMillis()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        logErrorEnriquecido(req, ex, 500, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, ex.getMessage(), System.currentTimeMillis()));
    }

    private void logErrorEnriquecido(HttpServletRequest req, Exception ex, int status, String mensaje) {
        String method = req.getMethod();
        String path = req.getRequestURI();
        String query = req.getQueryString();
        Map<String, String[]> paramMap = req.getParameterMap();
        StringBuilder params = new StringBuilder();
        if (!paramMap.isEmpty()) {
            params.append(" [params: ");
            paramMap.forEach((k, v) -> params.append(k).append("=").append(Arrays.toString(v)).append("; "));
            params.append("]");
        }
        String auth = req.getHeader("Authorization");
        String origin = req.getHeader("Origin");
        String userAgent = req.getHeader("User-Agent");
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
        log.error("[HTTP ERROR] {} {}{}{} [Origin: {}] [User-Agent: {}] [Usuario: {}] [Auth: {}] -> {} {}\nMensaje: {}\nStacktrace: ",
                method, path, query == null ? "" : "?" + query, params, origin, userAgent, usuario, auth != null ? "present" : "none", status, mensaje, ex);
    }
}
