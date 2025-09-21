package com.vzt.api.dtos.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MFAVerifyDTO {
    private String otp;
    private boolean trusted;
    private String username;
}
