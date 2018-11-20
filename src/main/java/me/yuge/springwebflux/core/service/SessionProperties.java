package me.yuge.springwebflux.core.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.session")
public class SessionProperties {
    private int maxIdleDays = 7;
    private String prefix = "session:";
    private String userSessionsPrefix = "sessions:";
}
