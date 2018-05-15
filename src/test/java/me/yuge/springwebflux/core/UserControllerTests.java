package me.yuge.springwebflux.core;

import me.yuge.springwebflux.core.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;


@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testGetAllUsers() {
        webTestClient.get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class);
    }

    @Test
    public void testGetByUsername() {
        webTestClient.get().uri("/users?username=foo")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("foo");
    }

}
