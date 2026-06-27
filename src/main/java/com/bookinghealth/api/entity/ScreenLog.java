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
@Table(name = "NHAT_KY_SANG_LOC")
public class ScreenLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maBanGhi", nullable = false)
  Long id;

  @ManyToOne
  @JoinColumn(name = "maNguoiDung")
  User user;

  @ManyToOne
  @JoinColumn(name = "maChuyenKhoaDeXuat")
  Specialty suggestedSpecialty;

  @Column(name = "trieuChung", length = 255)
  String symptoms;

  /** Thời điểm thực hiện sàng lọc (phục vụ thống kê / truy hồi theo thời gian). */
  @Column(name = "thoiGianSangLoc")
  LocalDateTime screenedAt;

  /** Câu trả lời AI đã phản hồi (lưu lại để xem lại / làm mẫu few-shot cho RAG). */
  @Column(name = "cauTraLoiAI", length = 2000)
  String aiAnswer;

  /**
   * Cờ cho phép dùng bản ghi này làm mẫu few-shot cho RAG (0/1). Mặc định 0 khi tạo; chỉ set = 1
   * khi NGƯỜI DÙNG bấm nút "Phản hồi tốt" cho câu trả lời (xác nhận ca này đúng/hữu ích).
   */
  @Column(name = "dungLamMauHuanLuyen", columnDefinition = "INT DEFAULT 0")
  Integer useForTraining;
}
