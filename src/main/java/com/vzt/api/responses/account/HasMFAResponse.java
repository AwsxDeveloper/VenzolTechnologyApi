package com.vzt.api.responses.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasMFAResponse {
    public boolean enabled;
}
