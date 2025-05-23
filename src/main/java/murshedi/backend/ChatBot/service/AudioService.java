package murshedi.backend.ChatBot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AudioService {

    @Value("${openai.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final Map<String, String> audioFiles = new ConcurrentHashMap<>();
    
    public AudioService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Convert text to audio using OpenAI's TTS API and store the file.
     * 
     * @param text The text to convert to speech
     * @return Response ID associated with the processed audio
     */
    public String processAudio(String text, String responseId) {
        String audioFilePath = "output_" + responseId + ".mp3";
        
        try {
            // Setup request to OpenAI TTS API
            String ttsUrl = "https://api.openai.com/v1/audio/speech";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "tts-1");
            requestBody.put("voice", "onyx");
            requestBody.put("input", text);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make request to OpenAI
            ResponseEntity<byte[]> response = restTemplate.exchange(
                ttsUrl, 
                HttpMethod.POST, 
                entity, 
                byte[].class
            );
            
            // Save the audio file
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                saveAudioFile(response.getBody(), audioFilePath);
                audioFiles.put(responseId, audioFilePath);
                return audioFilePath;
            } else {
                throw new RuntimeException("Failed to generate audio: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating audio: " + e.getMessage(), e);
        }
    }
    
    private void saveAudioFile(byte[] audioData, String filePath) throws IOException {
        File file = new File(filePath);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(audioData);
        }
    }
    
    /**
     * Retrieve the path of an audio file based on response ID
     */
    public String getAudioFilePath(String responseId) {
        return audioFiles.get(responseId);
    }
    
    /**
     * Generate a unique response ID
     */
    public String generateResponseId() {
        return String.valueOf(Instant.now().toEpochMilli());
    }
}
