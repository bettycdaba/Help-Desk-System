package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthControllerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void authenticationExceptionReturnsUnauthorizedResponse() {
        ResponseEntity<Map<String, Object>> response = exceptionHandler
                .handleAuthenticationException(new BadCredentialsException("bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().get("error"));
        assertEquals("Invalid email or password", response.getBody().get("message"));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().get("status"));
    }
}
