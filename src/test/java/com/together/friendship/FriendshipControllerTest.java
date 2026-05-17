package com.together.friendship;

import com.together.AbstractIntegrationTest;
import com.together.domain.friendship.FriendshipStatus;
import com.together.dto.auth.AuthResponse;
import com.together.dto.auth.RegisterRequest;
import com.together.dto.friendship.FriendshipDto;
import com.together.dto.friendship.SendFriendRequestDto;
import com.together.dto.friendship.UpdateFriendshipRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendshipControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String aliceToken;
    private UUID aliceId;
    private String bobToken;
    private UUID bobId;

    @BeforeAll
    void setup() {
        RegisterRequest aliceReg = new RegisterRequest(
                "alice@example.com", "password123", "Alice", "#CECBF6");
        ResponseEntity<AuthResponse> aliceResp = restTemplate.postForEntity(
                "/api/auth/register", aliceReg, AuthResponse.class);
        aliceToken = aliceResp.getBody().accessToken();
        aliceId = aliceResp.getBody().user().id();

        RegisterRequest bobReg = new RegisterRequest(
                "bob@example.com", "password123", "Bob", "#AABBCC");
        ResponseEntity<AuthResponse> bobResp = restTemplate.postForEntity(
                "/api/auth/register", bobReg, AuthResponse.class);
        bobToken = bobResp.getBody().accessToken();
        bobId = bobResp.getBody().user().id();
    }

    @Test
    void send_and_accept_friend_request() {
        // Use a distinct carol user to avoid state pollution between tests
        RegisterRequest carolReg = new RegisterRequest(
                "carol@example.com", "password123", "Carol", "#BBCCDD");
        ResponseEntity<AuthResponse> carolResp = restTemplate.postForEntity(
                "/api/auth/register", carolReg, AuthResponse.class);
        String carolToken = carolResp.getBody().accessToken();
        UUID carolId = carolResp.getBody().user().id();

        SendFriendRequestDto req = new SendFriendRequestDto(carolId);
        ResponseEntity<FriendshipDto> sendResp = restTemplate.postForEntity(
                "/api/friendships", new HttpEntity<>(req, bearerHeaders(aliceToken)), FriendshipDto.class);

        assertThat(sendResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(sendResp.getBody().status()).isEqualTo(FriendshipStatus.PENDING);

        UUID friendshipId = sendResp.getBody().id();
        UpdateFriendshipRequest acceptReq = new UpdateFriendshipRequest(FriendshipStatus.ACCEPTED);
        ResponseEntity<FriendshipDto> acceptResp = restTemplate.exchange(
                "/api/friendships/" + friendshipId, HttpMethod.PUT,
                new HttpEntity<>(acceptReq, bearerHeaders(carolToken)), FriendshipDto.class);

        assertThat(acceptResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acceptResp.getBody().status()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void decline_friend_request() {
        SendFriendRequestDto req = new SendFriendRequestDto(bobId);
        ResponseEntity<FriendshipDto> sendResp = restTemplate.postForEntity(
                "/api/friendships", new HttpEntity<>(req, bearerHeaders(aliceToken)), FriendshipDto.class);

        UUID friendshipId = sendResp.getBody().id();
        UpdateFriendshipRequest declineReq = new UpdateFriendshipRequest(FriendshipStatus.DECLINED);
        ResponseEntity<FriendshipDto> declineResp = restTemplate.exchange(
                "/api/friendships/" + friendshipId, HttpMethod.PUT,
                new HttpEntity<>(declineReq, bearerHeaders(bobToken)), FriendshipDto.class);

        assertThat(declineResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(declineResp.getBody().status()).isEqualTo(FriendshipStatus.DECLINED);
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
