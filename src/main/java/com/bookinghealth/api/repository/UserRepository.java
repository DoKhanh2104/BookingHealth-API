package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);

  // Return true if there is another user (not the given id) with the given email
  boolean existsByEmailAndIdNot(String email, Long id);

  Optional<User> findByPhone(String phone);

}
