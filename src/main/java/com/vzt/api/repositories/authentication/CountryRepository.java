package com.vzt.api.repositories.authentication;

import com.vzt.api.models.authentication.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCountryCode(String code);
}
