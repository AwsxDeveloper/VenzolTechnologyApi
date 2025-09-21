package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.MFASetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MFASettingRepository extends JpaRepository<MFASetting, Long> {
}
