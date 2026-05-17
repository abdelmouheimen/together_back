package com.together.item;

import com.together.AbstractIntegrationTest;
import com.together.domain.activity.ActivityEventRepository;
import com.together.dto.auth.AuthResponse;
import com.together.dto.auth.RegisterRequest;
import com.together.dto.item.CreateItemRequest;
import com.together.dto.item.TodoItemDto;
import com.together.dto.item.ToggleItemRequest;
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
class TodoItemControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActivityEventRepository activityEventRepository;

    private String token;
    private String listId;

    @BeforeAll
    void setup() {
        RegisterRequest reg = new RegisterRequest(
                "itemtest@example.com", "password123", "Item User", "#CECBF6");
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/auth/register", reg, AuthResponse.class);
        token = resp.getBody().accessToken();

        CreateListRequest listReq = new CreateListRequest("Test List", "📝", "#EEF2FF", "#4A3ABA", List.of());
        ResponseEntity<TodoListDto> listResp = restTemplate.postForEntity(
                "/api/lists", new HttpEntity<>(listReq, bearerHeaders()), TodoListDto.class);
        listId = listResp.getBody().id().toString();
    }

    @Test
    void create_item_and_toggle() {
        CreateItemRequest req = new CreateItemRequest("Buy milk");
        ResponseEntity<TodoItemDto> createResp = restTemplate.postForEntity(
                "/api/lists/" + listId + "/items",
                new HttpEntity<>(req, bearerHeaders()), TodoItemDto.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody().text()).isEqualTo("Buy milk");
        assertThat(createResp.getBody().done()).isFalse();

        String itemId = createResp.getBody().id().toString();
        long eventsBefore = activityEventRepository.count();

        ToggleItemRequest toggleReq = new ToggleItemRequest(true);
        ResponseEntity<TodoItemDto> toggleResp = restTemplate.exchange(
                "/api/lists/" + listId + "/items/" + itemId + "/toggle",
                HttpMethod.PATCH, new HttpEntity<>(toggleReq, bearerHeaders()), TodoItemDto.class);

        assertThat(toggleResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(toggleResp.getBody().done()).isTrue();
        assertThat(toggleResp.getBody().checkedBy()).isNotNull();

        long eventsAfter = activityEventRepository.count();
        assertThat(eventsAfter).isGreaterThan(eventsBefore);
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
