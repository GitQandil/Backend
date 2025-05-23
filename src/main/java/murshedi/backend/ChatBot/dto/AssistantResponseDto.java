package murshedi.backend.ChatBot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantResponseDto {
    private String answer;
    private String responseId;
    private String audioFile;
    private String threadId;
}
