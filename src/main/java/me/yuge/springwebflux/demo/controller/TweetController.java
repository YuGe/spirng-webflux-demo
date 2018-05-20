package me.yuge.springwebflux.demo.controller;

import me.yuge.springwebflux.core.exception.NotFoundStatusException;
import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Instant;

@RestController
@RequestMapping("tweets")
@PreAuthorize("isAuthenticated()")
public class TweetController {
    private final TweetRepository tweetRepository;

    @Autowired
    public TweetController(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public Flux<Tweet> getAllTweets(@RequestParam(name = "page", defaultValue = "0") Integer page,
                                    @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return tweetRepository.findAll().skip(size * page).take(size);
    }

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Mono<Tweet> get(@PathVariable String id) {
        return tweetRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundStatusException()));
    }

    @PostMapping
    public Mono<Tweet> create(@Valid @RequestBody Tweet tweet) {
        Instant now = Instant.now();
        tweet.setCreatedTime(now);
        tweet.setModifiedTime(now);
        return tweetRepository.save(tweet);
    }

    @PutMapping("{id}")
    public Mono<Tweet> update(@PathVariable String id, @Valid @RequestBody Tweet tweet) {
        return tweetRepository.findById(id).switchIfEmpty(
                Mono.error(new NotFoundStatusException())
        ).flatMap(
                oldTweet -> {
                    oldTweet.setText(tweet.getText());
                    oldTweet.setModifiedTime(Instant.now());
                    return tweetRepository.save(oldTweet);
                }
        );
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return tweetRepository.findById(id).switchIfEmpty(
                Mono.error(new NotFoundStatusException())
        ).flatMap(tweetRepository::delete);
    }

    // Tweets are Sent to the client as Server Sent Events
    @GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweets() {
        return tweetRepository.findAll();
    }
}
