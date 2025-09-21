package com.vzt.api.responses.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MFAResponse {
    private String uri;
    private String setupCode;
    private String deletionCode;
}
