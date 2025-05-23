package murshedi.backend.ChatBot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConversationDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("title")
    private String title;




    public ConversationDTO(String id, String title) {
        this.id = id;
        this.title = title;
    }



}
