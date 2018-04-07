package me.yuge.springwebflux.demo;

import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.Credentials.basicAuthenticationCredentials;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TweetControllerTests {

    @Autowired
    ApplicationContext context;

    @Autowired
    TweetRepository tweetRepository;

    private WebTestClient webTestClient;

    @Before
    public void setup() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .filter(basicAuthentication())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testCreateTweet() {
        Tweet tweet = new Tweet("This is a Test Tweet");

        webTestClient.post().uri("/tweets")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.text").isEqualTo("This is a Test Tweet");
    }

    @Test
    public void testGetAllTweets() {
        webTestClient.get().uri("/tweets")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Tweet.class);
    }

    @Test
    public void testGetSingleTweet() {
        Tweet tweet = tweetRepository.save(new Tweet("Hello, World!")).block();

        webTestClient.get()
                .uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response ->
                        Assertions.assertThat(response.getResponseBody()).isNotNull());
    }

    @Test
    public void testUpdateTweet() {
        Tweet tweet = tweetRepository.save(new Tweet("Initial Tweet")).block();

        Tweet newTweetData = new Tweet("Updated Tweet");

        webTestClient.put()
                .uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .attributes(adminCredentials())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(newTweetData), Tweet.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.text").isEqualTo("Updated Tweet");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteTweet() {
        Tweet tweet = tweetRepository.save(new Tweet("To be deleted")).block();

        webTestClient.delete()
                .uri("/tweets/{id}", Collections.singletonMap("id", tweet.getId()))
                .exchange()
                .expectStatus().isOk();
    }

    private Consumer<Map<String, Object>> adminCredentials() {
        return basicAuthenticationCredentials("admin", "FooBar123");
    }

}
