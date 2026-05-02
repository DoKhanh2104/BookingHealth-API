package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "VAI_TRO")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maVaiTro", nullable = false)
  Long id;

  @Column(name = "tenVaiTro", length = 10)
  String roleName;

  @Column(name = "moTa")
  String roleDescription;

  @ManyToMany(mappedBy = "roles")
  Set<User> users = new LinkedHashSet<>();
}
