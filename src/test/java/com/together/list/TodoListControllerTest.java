package com.together.list;

import com.together.AbstractIntegrationTest;
import com.together.dto.auth.AuthResponse;
import com.together.dto.auth.RegisterRequest;
import com.together.dto.list.CreateListRequest;
import com.together.dto.list.TodoListDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TodoListControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private String otherToken;

    @BeforeAll
    void setup() {
        RegisterRequest reg = new RegisterRequest(
                "listtest@example.com", "password123", "List User", "#CECBF6");
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/auth/register", reg, AuthResponse.class);
        token = resp.getBody().accessToken();

        RegisterRequest otherReg = new RegisterRequest(
                "listother@example.com", "password123", "Other User", "#AABBCC");
        ResponseEntity<AuthResponse> otherResp = restTemplate.postForEntity(
                "/api/auth/register", otherReg, AuthResponse.class);
        otherToken = otherResp.getBody().accessToken();
    }

    @Test
    void create_and_get_list() {
        CreateListRequest req = new CreateListRequest("Shopping", "🛒", "#EEF2FF", "#4A3ABA", List.of(), null);
        HttpEntity<CreateListRequest> entity = new HttpEntity<>(req, bearerHeaders(token));

        ResponseEntity<TodoListDto> createResp = restTemplate.postForEntity(
                "/api/lists", entity, TodoListDto.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).isNotNull();
        assertThat(createResp.getBody().name()).isEqualTo("Shopping");

        ResponseEntity<TodoListDto> getResp = restTemplate.exchange(
                "/api/lists/" + createResp.getBody().id(), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), TodoListDto.class);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().name()).isEqualTo("Shopping");
    }

    @Test
    void access_list_as_non_member_returns_403() {
        CreateListRequest req = new CreateListRequest("Private List", "📋", "#EEF2FF", "#4A3ABA", List.of(), null);
        ResponseEntity<TodoListDto> createResp = restTemplate.postForEntity(
                "/api/lists", new HttpEntity<>(req, bearerHeaders(token)), TodoListDto.class);

        ResponseEntity<String> getResp = restTemplate.exchange(
                "/api/lists/" + createResp.getBody().id(), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(otherToken)), String.class);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private HttpHeaders bearerHeaders(String t) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(t);
        return headers;
    }
}
