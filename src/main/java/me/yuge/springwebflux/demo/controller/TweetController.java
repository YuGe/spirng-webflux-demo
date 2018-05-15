package me.yuge.springwebflux.demo.controller;

import me.yuge.springwebflux.demo.model.Tweet;
import me.yuge.springwebflux.demo.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public Mono<Tweet> createTweets(@Valid @RequestBody Tweet tweet) {
        Instant now = Instant.now();
        tweet.setCreatedTime(now);
        tweet.setModifiedTime(now);
        return tweetRepository.save(tweet);
    }

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String tweetId) {
        return tweetRepository.findById(tweetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable(value = "id") String tweetId,
                                                   @Valid @RequestBody Tweet tweet) {
        return tweetRepository.findById(tweetId)
                .flatMap(existingTweet -> {
                    existingTweet.setText(tweet.getText());
                    return tweetRepository.save(existingTweet);
                })
                .map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteTweet(@PathVariable(value = "id") String tweetId) {

        return tweetRepository.findById(tweetId)
                .flatMap(existingTweet ->
                        tweetRepository.delete(existingTweet)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Tweets are Sent to the client as Server Sent Events
    @GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweets() {
        return tweetRepository.findAll();
    }

}
