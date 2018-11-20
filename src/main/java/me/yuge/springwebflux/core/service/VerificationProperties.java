package me.yuge.springwebflux.core.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.verification")
public class VerificationProperties {
    private int maxIdleMinutes = 0;
    private String codePrefix = "code:";
    private String verifiedPrefix = "verified:";
}
