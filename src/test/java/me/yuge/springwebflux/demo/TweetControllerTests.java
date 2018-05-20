package me.yuge.springwebflux.demo;

import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest
public class TweetControllerTests {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private TweetRepository tweetRepository;

    @Test
    @WithMockUser
    public void testCreate() {
        Tweet tweet = new Tweet("This is a Test Tweet");

        webTestClient.post().uri("/tweets")
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.text").isEqualTo("This is a Test Tweet");
    }

    @Test
    public void testGetAll() {
        webTestClient.get().uri("/tweets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Tweet.class);
    }

    @Test
    public void testGetSingle() {
        Tweet tweet = tweetRepository.save(new Tweet("Hello, World!")).block(Duration.ofSeconds(5));
        Objects.requireNonNull(tweet);

        webTestClient.get().uri("/tweets/{id}", Maps.newHashMap("id", tweet.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(
                        response -> Assertions.assertThat(response.getResponseBody()).isNotNull()
                );
    }

    @Test
    @WithMockUser
    public void testUpdate() {
        Tweet tweet = tweetRepository.save(new Tweet("Initial Tweet")).block(Duration.ofSeconds(5));
        Objects.requireNonNull(tweet);

        tweet.setText("Updated Tweet");

        webTestClient.put().uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo("Updated Tweet");
    }

    @Test
    @WithMockUser
    public void testDelete() {
        Tweet tweet = tweetRepository.save(new Tweet("To be deleted")).block(Duration.ofSeconds(5));
        Objects.requireNonNull(tweet);

        webTestClient.delete().uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .exchange()
                .expectStatus().isOk();
    }
}
