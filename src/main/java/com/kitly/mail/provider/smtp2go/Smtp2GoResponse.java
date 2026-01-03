package com.kitly.mail.provider.smtp2go;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Smtp2GoResponse {
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("data")
    private ResponseData data;
    
    @Data
    public static class ResponseData {
        private Boolean succeeded;
        
        @JsonProperty("message_id")
        private String messageId;
    }
}
