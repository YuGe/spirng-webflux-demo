package me.yuge.springwebflux.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix="application")
public class ApplicationProperties {

    private Session session = new Session();

    @Getter
    @Setter
    public static class Session {
        private int timeout = 7;
    }
}
