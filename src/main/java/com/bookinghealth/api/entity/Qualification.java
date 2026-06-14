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

  @Column(name = "duongDanTep")
  String attachmentUrl;

  /**
   * Trạng thái duyệt của chứng chỉ:
   * 0 - Chờ duyệt
   * 1 - Đã duyệt
   * 2 - Từ chối
   */
  @Column(name = "trangThai", nullable = false, columnDefinition = "INT DEFAULT 0")
  Integer status = 0;

  @ManyToOne
  @JoinColumn(name = "maBacSi")
  Doctor doctor;
}
