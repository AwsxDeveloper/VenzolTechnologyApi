package com.vzt.api.dtos.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallBackDTO {
    private String code;
    private String realmId;
    private String secret;
}
