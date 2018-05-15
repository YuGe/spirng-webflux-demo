package me.yuge.springwebflux.core;

import me.yuge.springwebflux.core.model.Session;
import me.yuge.springwebflux.core.service.SessionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Base64;
import java.util.Objects;


@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest
public class SessionControllerTests {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private SessionService sessionService;

    final private static String AUTH_HEADER = "Basic "
            + Base64.getEncoder().encodeToString("admin:FooBar123".getBytes());

    @Test
    public void testPost() {
        webTestClient.post().uri("/session")
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.login").isEqualTo("admin");
    }

    @Test
    public void testGet() {
        String id = "toGet";
        Session session = Session.builder().id(id).maxIdleTime(Duration.ofSeconds(5)).build();
        session = sessionService.save(session).block(Duration.ofSeconds(5));
        Objects.requireNonNull(session);

        webTestClient.get().uri("/session/" + session.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id);
    }

    @Test
    public void testDelete() {
        String id = "toDelete";
        Session session = Session.builder().id(id).maxIdleTime(Duration.ofSeconds(5)).build();
        session = sessionService.save(session).block(Duration.ofSeconds(5));
        Objects.requireNonNull(session);

        webTestClient.delete().uri("/session/" + session.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.getId())
                .exchange()
                .expectStatus().isOk();
    }
}
