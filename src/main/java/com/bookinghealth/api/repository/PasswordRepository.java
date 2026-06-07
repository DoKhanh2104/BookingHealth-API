package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.PasswordReset;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<PasswordReset, Integer> {
  Optional<PasswordReset> findByToken(String token);
}
