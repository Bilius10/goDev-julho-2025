package br.com.senior.transport_logistics.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder

                .defaultHeader("User-Agent", "transport_logistics/1.0 (teste@exemplo.com)")
                .build();
    }
}

