package me.yuge.springwebflux.demo;

import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.Collections;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TweetControllerTests {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private TweetRepository tweetRepository;

    private WebTestClient webTestClient;

    @Before
    public void setup() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreateTweet() {
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
    public void testGetAllTweets() {
        webTestClient.get().uri("/tweets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Tweet.class);
    }

    @Test
    public void testGetSingleTweet() {
        Tweet tweet = tweetRepository.save(new Tweet("Hello, World!")).block();
        Assert.notNull(tweet, "tweet should not be null");

        webTestClient.get().uri("/tweets/{id}", Maps.newHashMap("id", tweet.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(
                        response -> Assertions.assertThat(response.getResponseBody()).isNotNull()
                );
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testUpdateTweet() {
        Tweet tweet = tweetRepository.save(new Tweet("Initial Tweet")).block();
        Assert.notNull(tweet, "tweet should not be null");

        tweet.setText("Updated Tweet");

        webTestClient.put().uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo("Updated Tweet");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteTweet() {
        Tweet tweet = tweetRepository.save(new Tweet("To be deleted")).block();
        Assert.notNull(tweet, "tweet should not be null");

        webTestClient.delete().uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .exchange()
                .expectStatus().isOk();
    }
}
