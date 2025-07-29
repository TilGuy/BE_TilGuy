package com.tilguys.matilda.common.external.config;

import com.tilguys.matilda.common.external.AIClient;
import com.tilguys.matilda.common.external.ClaudeClient;
import com.tilguys.matilda.common.external.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AIClients {

    @Bean
    public List<AIClient> aiClients(@Autowired OpenAIClient openAIClient, @Autowired ClaudeClient claudeClient) {
        return List.of(openAIClient, claudeClient);
    }
}
