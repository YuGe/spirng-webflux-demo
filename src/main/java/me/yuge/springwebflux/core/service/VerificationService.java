package me.yuge.springwebflux.core.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import me.yuge.springwebflux.core.model.Verification;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;

@Service
public class VerificationService {
    private final EmailService emailService;
    private final Configuration configuration;
    private final VerificationProperties verificationProperties;
    private final ReactiveRedisOperations<String, String> stringOperations;

    public VerificationService(EmailService emailService,
                               Configuration configuration,
                               VerificationProperties verificationProperties,
                               ReactiveRedisOperations<String, String> stringOperations) {
        this.emailService = emailService;
        this.configuration = configuration;
        this.verificationProperties = verificationProperties;
        this.stringOperations = stringOperations;
    }

    public Mono<Verification> create(Verification verification) {
        Duration maxIdleTime = Duration.ofMinutes(verificationProperties.getMaxIdleMinutes());
        String redisKey = getCodeRedisKey(verification.getLogin());
        verification.setCode(Verification.nextCode());

        return stringOperations.opsForValue()
                .set(redisKey, verification.getCode(), maxIdleTime)
                .filter(Boolean::booleanValue)
                .map(succeed -> verification);
    }

    public Mono<Boolean> emailCode(Verification verification) {
        final Writer writer = new StringWriter();
        try {
            Template template = configuration.getTemplate("email/verification.ftlh");
            template.process(verification, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
        return Mono.fromCallable(() -> {
            emailService.sendHtmlMessage(verification.getLogin(), "Verification Code", writer.toString());
            return true;
        }).subscribeOn(Schedulers.elastic());
    }

    public Mono<Boolean> verify(Verification verification) {
        String redisKey = getCodeRedisKey(verification.getLogin());

        return stringOperations.opsForValue()
                .get(redisKey)
                .map(token -> token.equals(verification.getLogin()))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.just(Boolean.FALSE))
                .doOnNext(matched -> stringOperations.opsForValue().delete(redisKey));
    }

    private String getCodeRedisKey(String value) {
        return verificationProperties.getCodePrefix() + value;
    }
}
