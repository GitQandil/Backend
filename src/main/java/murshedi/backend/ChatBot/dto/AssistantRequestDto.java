package murshedi.backend.ChatBot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantRequestDto {
    private String question;
    private Boolean blindMode = false;
    private String threadId;
}
