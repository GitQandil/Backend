// src/main/java/murshedi/backend/ChatBot/service/AssistantService.java
package murshedi.backend.ChatBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.service.OpenAiService;
import murshedi.backend.ChatBot.config.OpenAiConfig;
import murshedi.backend.ChatBot.dto.AssistantRequestDto;
import murshedi.backend.ChatBot.dto.AssistantResponseDto;
import murshedi.backend.ChatBot.dto.FileUploadResponseDto;
import murshedi.backend.ChatBot.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    private static final String OPENAI_API_BASE_URL = "https://api.openai.com/v1";

    private final OpenAiService openAiService;
    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;
    private final AudioService audioService;
    private final ObjectMapper objectMapper;
    private final Map<String, Map<String, Object>> responses = new ConcurrentHashMap<>();
    private final Map<String, String> threads = new ConcurrentHashMap<>();

    public AssistantService(OpenAiService openAiService,
                            OpenAiConfig openAiConfig,
                            RestTemplate restTemplate,
                            AudioService audioService,
                            ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.openAiConfig = openAiConfig;
        this.restTemplate = restTemplate;
        this.audioService = audioService;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a question using OpenAI's Assistant API.
     */
    public AssistantResponseDto processQuestion(AssistantRequestDto requestDto) {
        try {
            String userQuestion = requestDto.getQuestion();
            boolean blindMode = Boolean.TRUE.equals(requestDto.getBlindMode());
            String threadId = requestDto.getThreadId();

            if (userQuestion == null || userQuestion.isEmpty()) {
                throw new IllegalArgumentException("No question provided");
            }

            logger.info("Processing question. Thread ID: {}, Blind Mode: {}", threadId, blindMode);

            Map<String, Object> threadResponse;
            if (threadId == null || threadId.isEmpty()) {
                threadResponse = createThread(userQuestion);
                threadId = (String) threadResponse.get("id");
            } else {
                addMessageToThread(threadId, userQuestion);
            }

            Map<String, Object> runResponse = runThread(threadId);
            String runId = (String) runResponse.get("id");

            // Poll until completion
            while (true) {
                Map<String, Object> runStatus = getRunStatus(threadId, runId);
                String status = (String) runStatus.get("status");
                logger.info("Run Status: {}", status);

                if ("completed".equals(status)) {
                    break;
                } else if ("failed".equals(status)) {
                    throw new RuntimeException("Assistant run failed: " + runStatus.get("last_error"));
                }

                Thread.sleep(1000);
            }

            // Extract the assistant's reply
            List<Map<String, Object>> messages = getThreadMessages(threadId);
            String assistantMessage = extractLatestAssistantMessage(messages);
            assistantMessage = TextUtils.cleanText(assistantMessage);

            // Generate and store response
            String responseId = audioService.generateResponseId();
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("answer", assistantMessage);
            responseData.put("audio_file", null);
            responses.put(responseId, responseData);

            // Optionally synthesize audio
            String audioFilePath = null;
            if (blindMode) {
                audioFilePath = audioService.processAudio(assistantMessage, responseId);
                responseData.put("audio_file", audioFilePath);
            }

            return AssistantResponseDto.builder()
                .answer(assistantMessage)
                .responseId(responseId)
                .audioFile(audioFilePath != null ? "/api/audio/" + audioFilePath : null)
                .threadId(threadId)
                .build();

        } catch (Exception e) {
            logger.error("Error processing question", e);
            throw new RuntimeException("Failed to process question: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a previously generated response by ID.
     */
    public AssistantResponseDto getResponse(String responseId) {
        Map<String, Object> responseData = responses.get(responseId);
        if (responseData == null) {
            throw new IllegalArgumentException("Invalid response ID: " + responseId);
        }
        String answer = (String) responseData.get("answer");
        String audioFile = (String) responseData.get("audio_file");
        if (audioFile == null) {
            return AssistantResponseDto.builder()
                .answer(answer)
                .responseId(responseId)
                .build();
        }
        return AssistantResponseDto.builder()
            .answer(answer)
            .responseId(responseId)
            .audioFile("/api/audio/" + audioFile)
            .build();
    }

    /**
     * Upload files to the assistant for vector search.
     */
    public FileUploadResponseDto uploadFilesToAssistant(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided");
        }
        try {
            Map<String, Object> assistant = getAssistant(openAiConfig.getAssistantId());
            String currentVectorStoreId = extractVectorStoreId(assistant);

            List<String> uploadedFileIds = new ArrayList<>();
            List<String> uploadedFilenames = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileId = uploadFile(file);
                    uploadedFileIds.add(fileId);
                    uploadedFilenames.add(file.getOriginalFilename());
                }
            }

            // Merge with existing files
            List<String> allFileIds = new ArrayList<>();
            if (currentVectorStoreId != null && !currentVectorStoreId.isEmpty()) {
                Map<String, Object> vectorStoreFiles = getVectorStoreFiles(currentVectorStoreId);
                List<Map<String, Object>> data = (List<Map<String, Object>>) vectorStoreFiles.get("data");
                if (data != null) {
                    for (Map<String, Object> fileInfo : data) {
                        allFileIds.add((String) fileInfo.get("id"));
                    }
                }
            }
            allFileIds.addAll(uploadedFileIds);

            Map<String, Object> newVectorStore = createVectorStore(allFileIds);
            String newVectorStoreId = (String) newVectorStore.get("id");
            updateAssistantVectorStore(openAiConfig.getAssistantId(), newVectorStoreId);

            return FileUploadResponseDto.builder()
                .message(uploadedFileIds.size() + " files uploaded successfully")
                .uploadedFileIds(uploadedFileIds)
                .uploadedFilenames(uploadedFilenames)
                .previousVectorStoreId(currentVectorStoreId)
                .newVectorStoreId(newVectorStoreId)
                .assistantUpdated(true)
                .totalFiles(allFileIds.size())
                .build();

        } catch (Exception e) {
            logger.error("Error uploading files to assistant", e);
            throw new RuntimeException("Failed to upload files: " + e.getMessage(), e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Private helper methods follow:
    // ────────────────────────────────────────────────────────────────────────────

    private Map<String, Object> createThread(String initialMessage) {
        HttpHeaders headers = createOpenAiHeaders();
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", initialMessage);
        messages.add(message);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/threads",
            HttpMethod.POST,
            request,
            Map.class
        );
        return response.getBody();
    }

    private void addMessageToThread(String threadId, String messageContent) {
        HttpHeaders headers = createOpenAiHeaders();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("role", "user");
        requestBody.put("content", messageContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        restTemplate.exchange(
            OPENAI_API_BASE_URL + "/threads/" + threadId + "/messages",
            HttpMethod.POST,
            request,
            Map.class
        );
    }

    private Map<String, Object> runThread(String threadId) {
        HttpHeaders headers = createOpenAiHeaders();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("assistant_id", openAiConfig.getAssistantId());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/threads/" + threadId + "/runs",
            HttpMethod.POST,
            request,
            Map.class
        );
        return response.getBody();
    }

    private Map<String, Object> getRunStatus(String threadId, String runId) {
        HttpHeaders headers = createOpenAiHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/threads/" + threadId + "/runs/" + runId,
            HttpMethod.GET,
            entity,
            Map.class
        );
        return response.getBody();
    }

    private List<Map<String, Object>> getThreadMessages(String threadId) {
        HttpHeaders headers = createOpenAiHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/threads/" + threadId + "/messages",
            HttpMethod.GET,
            entity,
            Map.class
        );
        Map<String, Object> body = response.getBody();
        return (List<Map<String, Object>>) body.get("data");
    }

    private String extractLatestAssistantMessage(List<Map<String, Object>> messages) {
        for (Map<String, Object> message : messages) {
            if ("assistant".equals(message.get("role"))) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) message.get("content");
                for (Map<String, Object> c : content) {
                    if ("text".equals(c.get("type"))) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> textValue = (Map<String, Object>) c.get("text");
                        return (String) textValue.get("value");
                    }
                }
            }
        }
        return "";
    }

    private Map<String, Object> getAssistant(String assistantId) {
        HttpHeaders headers = createOpenAiHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/assistants/" + assistantId,
            HttpMethod.GET,
            entity,
            Map.class
        );
        return response.getBody();
    }

    private String extractVectorStoreId(Map<String, Object> assistant) {
        if (assistant.containsKey("tool_resources")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> toolResources = (Map<String, Object>) assistant.get("tool_resources");
            if (toolResources != null && toolResources.containsKey("file_search")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> fileSearch = (Map<String, Object>) toolResources.get("file_search");
                @SuppressWarnings("unchecked")
                List<String> vectorStoreIds = (List<String>) fileSearch.get("vector_store_ids");
                if (vectorStoreIds != null && !vectorStoreIds.isEmpty()) {
                    return vectorStoreIds.get(0);
                }
            }
        }
        return null;
    }

    private String uploadFile(MultipartFile file) throws IOException {
        HttpHeaders headers = createOpenAiHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("purpose", "assistants");
        body.add("file", new HttpEntity<>(file.getBytes(), createFileHeaders(file.getOriginalFilename())));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/files",
            HttpMethod.POST,
            requestEntity,
            Map.class
        );
        return (String) response.getBody().get("id");
    }

    private Map<String, Object> getVectorStoreFiles(String vectorStoreId) {
        HttpHeaders headers = createOpenAiHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/vector_stores/" + vectorStoreId + "/files",
            HttpMethod.GET,
            entity,
            Map.class
        );
        return response.getBody();
    }

    private Map<String, Object> createVectorStore(List<String> fileIds) {
        HttpHeaders headers = createOpenAiHeaders();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("file_ids", fileIds);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            OPENAI_API_BASE_URL + "/vector_stores",
            HttpMethod.POST,
            request,
            Map.class
        );
        return response.getBody();
    }

    private void updateAssistantVectorStore(String assistantId, String vectorStoreId) {
        HttpHeaders headers = createOpenAiHeaders();
        Map<String, Object> fileSearch = new HashMap<>();
        fileSearch.put("vector_store_ids", Collections.singletonList(vectorStoreId));
        Map<String, Object> toolResources = new HashMap<>();
        toolResources.put("file_search", fileSearch);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tool_resources", toolResources);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        restTemplate.exchange(
            OPENAI_API_BASE_URL + "/assistants/" + assistantId,
            HttpMethod.POST,
            request,
            Map.class
        );
    }

    /**
     * Construct headers for any OpenAI request, using the raw API key
     * from OpenAiConfig rather than a non-existent getToken().
     */
    private HttpHeaders createOpenAiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());
        headers.set("OpenAI-Beta", "assistants=v1");
        return headers;
    }

    private HttpHeaders createFileHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("file", filename);
        return headers;
    }
}
