package com.bookinghealth.api.configuration;

import com.bookinghealth.api.constant.PredefinedRole;
import com.bookinghealth.api.entity.Role;
import com.bookinghealth.api.entity.User;
import com.bookinghealth.api.repository.RoleRepository;
import com.bookinghealth.api.repository.UserRepository;
import java.util.HashSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

  PasswordEncoder passwordEncoder;

  @NonFinal static final String ADMIN_USERNAME = "admin";

  @NonFinal static final String ADMIN_PASSWORD = "khanh2104";

  @Bean
  @ConditionalOnProperty(
      prefix = "spring",
      value = "datasource.driverClassName",
      havingValue = "com.mysql.cj.jdbc.Driver")
  ApplicationRunner applicationRunner(
      UserRepository userRepository, RoleRepository roleRepository) {
    return args -> {
      if (userRepository.findByPhone(ADMIN_USERNAME).isEmpty()) {
        roleRepository.save(
            Role.builder().roleName(PredefinedRole.USER_ROLE).roleDescription("User role").build());

        roleRepository.save(
            Role.builder().roleName(PredefinedRole.DOCTOR_ROLE).roleDescription("Doctor role").build());

        Role adminRole =
            roleRepository.save(
                Role.builder()
                    .roleName(PredefinedRole.ADMIN_ROLE)
                    .roleDescription("Admin role")
                    .build());

        var roles = new HashSet<Role>();

        roles.add(adminRole);

        User user =
            User.builder()
                .phone(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .build();

        userRepository.save(user);

        log.warn("Admin user created");
      }
      log.info("Application initialization completed .....");
    };
  }
}
