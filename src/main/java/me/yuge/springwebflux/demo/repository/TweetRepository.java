package me.yuge.springwebflux.demo.repository;

import me.yuge.springwebflux.demo.model.Tweet;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TweetRepository extends ReactiveSortingRepository<Tweet, String> {

}
