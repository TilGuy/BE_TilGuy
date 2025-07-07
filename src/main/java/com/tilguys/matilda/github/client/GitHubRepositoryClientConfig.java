package com.tilguys.matilda.github.client;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class GitHubRepositoryClientConfig {

    private final GitHubHeaderRequestInterceptor requestInterceptor;

    @Bean
    public RestTemplate gitHubRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(requestInterceptor));
        return restTemplate;
    }
}
