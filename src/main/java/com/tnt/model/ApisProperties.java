package com.tnt.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConfigurationProperties
@Getter
@Setter
@EnableScheduling
public class ApisProperties {

    private ApiProperties pricing;
    private ApiProperties track;
    private ApiProperties shipments;

    @Data
    public static class ApiProperties {
        private String baseUrl;
        private String uri;
    }
}
