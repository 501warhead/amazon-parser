package io.github.war501head.amazonparser.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * The web-based configuration for Spring Boot
 *
 * @author Sean K.
 */
@Configuration
public class WebConfiguration {

    /**
     * Create a RestTemplate Bean for use in our services
     *
     * @param builder The RestTemplateBuilder
     * @return The RestTemplate Bean
     */
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
