package com.vzt.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse<T> {
    private ResponseStatus status;
    private String message;
    private LocalDateTime timestamp;
    @JsonProperty("access_token")
    private String accessToken;
    private T data;
}
