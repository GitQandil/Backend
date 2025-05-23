package murshedi.backend.ChatBot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponseDto {
    private String message;
    private List<String> uploadedFileIds;
    private List<String> uploadedFilenames;
    private String previousVectorStoreId;
    private String newVectorStoreId;
    private Boolean assistantUpdated;
    private Integer totalFiles;
}
