package murshedi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import murshedi.backend.ChatBot.service.AudioService;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Inject the RestTemplate into AudioService’s constructor
    @Bean
    public AudioService audioService(RestTemplate restTemplate) {
        return new AudioService(restTemplate);
    }
}
