package com.vzt.api.dtos.realm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddNewRealmAdminDTO {
    private String uid;
    private String realmId;
}
