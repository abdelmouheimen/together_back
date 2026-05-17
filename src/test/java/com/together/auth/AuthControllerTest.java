package com.together.auth;

import com.together.AbstractIntegrationTest;
import com.together.dto.auth.AuthResponse;
import com.together.dto.auth.LoginRequest;
import com.together.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "password123", "Test User", "#CECBF6");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().user().email()).isEqualTo("test@example.com");
        assertThat(response.getBody().user().initials()).isEqualTo("TU");
    }

    @Test
    void register_duplicate_email_returns_409() {
        RegisterRequest request = new RegisterRequest(
                "duplicate@example.com", "password123", "Dup User", "#CECBF6");
        restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_success() {
        RegisterRequest reg = new RegisterRequest(
                "login@example.com", "password123", "Login User", "#AABBCC");
        restTemplate.postForEntity("/api/auth/register", reg, AuthResponse.class);

        LoginRequest login = new LoginRequest("login@example.com", "password123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", login, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
    }

    @Test
    void login_wrong_password_returns_401() {
        RegisterRequest reg = new RegisterRequest(
                "wrongpwd@example.com", "password123", "Wrong User", "#AABBCC");
        restTemplate.postForEntity("/api/auth/register", reg, AuthResponse.class);

        LoginRequest login = new LoginRequest("wrongpwd@example.com", "wrongpassword");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", login, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void access_protected_endpoint_without_token_returns_403() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
