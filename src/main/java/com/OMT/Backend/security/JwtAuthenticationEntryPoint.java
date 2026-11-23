package com.OMT.Backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        boolean isExpired = Boolean.TRUE.equals(request.getAttribute("jwt_expired"));
        boolean isInvalid = Boolean.TRUE.equals(request.getAttribute("jwt_invalid"));

        String error;
        String message;

        if (isExpired) {
            log.warn("Session timeout on request to {}: JWT token has expired", request.getServletPath());
            error = "Session Timeout";
            message = "Your session has timed out due to inactivity. Please log in again to continue.";
        } else if (isInvalid) {
            log.warn("Invalid JWT token on request to {}", request.getServletPath());
            error = "Unauthorized";
            message = "Authentication token is invalid or tampered.";
        } else {
            log.warn("Unauthorized access attempt to {}: {}", request.getServletPath(), authException.getMessage());
            error = "Unauthorized";
            message = "Full authentication is required. Authentication token is missing.";
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", error);
        body.put("message", message);
        body.put("sessionExpired", isExpired);
        body.put("path", request.getServletPath());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
