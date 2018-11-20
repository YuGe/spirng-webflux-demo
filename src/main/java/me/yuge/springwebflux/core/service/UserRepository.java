package me.yuge.springwebflux.core.service;

import me.yuge.springwebflux.core.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Mono<User> findByLogin(String login);

}
