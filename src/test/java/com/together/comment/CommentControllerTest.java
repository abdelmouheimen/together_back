package com.together.comment;

import com.together.AbstractIntegrationTest;
import com.together.dto.auth.AuthResponse;
import com.together.dto.auth.RegisterRequest;
import com.together.dto.comment.CommentDto;
import com.together.dto.comment.CreateCommentRequest;
import com.together.dto.item.CreateItemRequest;
import com.together.dto.item.TodoItemDto;
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
class CommentControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private String listId;
    private String itemId;

    @BeforeAll
    void setup() {
        RegisterRequest reg = new RegisterRequest(
                "commenttest@example.com", "password123", "Comment User", "#CECBF6");
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/auth/register", reg, AuthResponse.class);
        token = resp.getBody().accessToken();

        CreateListRequest listReq = new CreateListRequest("Comment List", "💬", "#EEF2FF", "#4A3ABA", List.of());
        ResponseEntity<TodoListDto> listResp = restTemplate.postForEntity(
                "/api/lists", new HttpEntity<>(listReq, bearerHeaders()), TodoListDto.class);
        listId = listResp.getBody().id().toString();

        CreateItemRequest itemReq = new CreateItemRequest("Item to comment on");
        ResponseEntity<TodoItemDto> itemResp = restTemplate.postForEntity(
                "/api/lists/" + listId + "/items",
                new HttpEntity<>(itemReq, bearerHeaders()), TodoItemDto.class);
        itemId = itemResp.getBody().id().toString();
    }

    @Test
    void add_and_delete_comment() {
        String url = "/api/lists/" + listId + "/items/" + itemId + "/comments";
        CreateCommentRequest req = new CreateCommentRequest("Great idea!");
        ResponseEntity<CommentDto> createResp = restTemplate.postForEntity(
                url, new HttpEntity<>(req, bearerHeaders()), CommentDto.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody().text()).isEqualTo("Great idea!");

        String commentId = createResp.getBody().id().toString();
        ResponseEntity<Void> deleteResp = restTemplate.exchange(
                url + "/" + commentId, HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders()), Void.class);

        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
