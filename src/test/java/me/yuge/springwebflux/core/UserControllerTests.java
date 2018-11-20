package me.yuge.springwebflux.core;

import me.yuge.springwebflux.core.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser
    public void testGetAllUsers() {
        webTestClient.get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class);
    }

    @Test
    @WithMockUser
    public void testGetByLogin() {
        webTestClient.get().uri("/users?login=admin@bar.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("admin");
    }
}
