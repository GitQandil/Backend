package murshedi.backend.ChatBot.controller;

import murshedi.backend.ChatBot.dto.AssistantRequestDto;
import murshedi.backend.ChatBot.dto.AssistantResponseDto;
import murshedi.backend.ChatBot.dto.FileUploadResponseDto;
import murshedi.backend.ChatBot.service.AssistantService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    /**
     * Endpoint to handle user questions to the AI assistant.
     * This replaces the Flask /ask endpoint.
     */
    @PostMapping("/assistant/ask")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<AssistantResponseDto> askQuestion(@RequestBody AssistantRequestDto requestDto) {
        AssistantResponseDto response = assistantService.processQuestion(requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve a specific response by ID.
     * This replaces the Flask /get_response/<response_id> endpoint.
     */
    @GetMapping("/assistant/response/{responseId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<AssistantResponseDto> getResponse(@PathVariable String responseId) {
        AssistantResponseDto response = assistantService.getResponse(responseId);
        
        // If audio is still processing, return appropriate status
        if (response.getAudioFile() == null && response.getAnswer() != null) {
            return ResponseEntity.accepted().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Upload files to be used by the AI assistant.
     * This replaces the Flask /upload endpoint.
     */
    @PostMapping("/assistant/upload")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<FileUploadResponseDto> uploadFiles(@RequestParam("files[]") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        
        FileUploadResponseDto response = assistantService.uploadFilesToAssistant(files);
        return ResponseEntity.ok(response);
    }

    /**
     * Stream audio file for playback.
     * This replaces the Flask /audio/<filename> endpoint.
     */
    @GetMapping("/audio/{filename}")
    public ResponseEntity<Resource> getAudio(@PathVariable String filename) {
        try {
            Path path = Paths.get(filename);
            File file = path.toFile();
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
