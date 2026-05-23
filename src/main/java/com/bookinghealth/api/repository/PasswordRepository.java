package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordRepository extends JpaRepository<PasswordReset, Integer> {
    Optional<PasswordReset> findByToken(String token);
}
