package me.yuge.springwebflux.demo.repository;

import me.yuge.springwebflux.demo.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
