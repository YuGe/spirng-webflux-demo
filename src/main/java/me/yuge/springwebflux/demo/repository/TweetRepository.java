package me.yuge.springwebflux.demo.repository;

import me.yuge.springwebflux.demo.model.Tweet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {

}
