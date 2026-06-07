package com.bookinghealth.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "TRINH_DO")
public class Qualification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maTrinhDo", nullable = false)
  Long id;

  @Column(name = "tenTrinhDo")
  String qualificationName;

  @Column(name = "ngayCap")
  LocalDateTime issueDate;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;
}
