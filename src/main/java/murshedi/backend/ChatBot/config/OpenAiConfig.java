// src/main/java/murshedi/backend/ChatBot/config/OpenAiConfig.java
package murshedi.backend.ChatBot.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.assistant.id}")
    private String assistantId;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey);
    }

    /**
     * Exposes the raw OpenAI API key for use in custom header construction.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Exposes the Assistant ID from configuration.
     */
    public String getAssistantId() {
        return assistantId;
    }
}
