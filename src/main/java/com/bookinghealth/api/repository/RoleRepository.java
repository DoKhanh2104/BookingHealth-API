package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByRoleName(String roleName);

  // Find roles by a set of role names (e.g., ["USER", "ADMIN"])
  List<Role> findAllByRoleNameIn(Set<String> roleNames);
}
