-- Create Price History table if not exists
CREATE TABLE IF NOT EXISTS lich_su_gia_kham (
  ma_lich_su_gia_kham BIGINT AUTO_INCREMENT PRIMARY KEY,
  chi_phi_kham DOUBLE NOT NULL,
  ngay_ap_dung DATETIME DEFAULT NULL,
  trang_thai INT DEFAULT NULL,
  ma_bac_si BIGINT,
  FOREIGN KEY (ma_bac_si) REFERENCES bac_si(ma_bac_si) ON DELETE CASCADE
);

-- Clean up schedules and related data
DELETE FROM khung_gio;
DELETE FROM lich_lam_viec;
DELETE FROM trinh_do;
DELETE FROM danh_gia_bac_si;
DELETE FROM lich_su_gia_kham;

-- 0. Khung giờ chuẩn (admin) — chỉ ~12–15 bản ghi cố định
DELETE FROM ca_kham_chuan;

INSERT INTO ca_kham_chuan (thoi_gian_bat_dau, thoi_gian_ket_thuc, trang_thai) VALUES
('08:00:00', '08:30:00', 1),
('08:30:00', '09:00:00', 1),
('09:00:00', '09:30:00', 1),
('09:30:00', '10:00:00', 1),
('10:00:00', '10:30:00', 1),
('10:30:00', '11:00:00', 1),
('13:30:00', '14:00:00', 1),
('14:00:00', '14:30:00', 1),
('14:30:00', '15:00:00', 1),
('15:00:00', '15:30:00', 1),
('15:30:00', '16:00:00', 1),
('16:00:00', '16:30:00', 1);

-- 1. Lịch làm việc: mỗi bác sĩ × 7 ngày
INSERT INTO lich_lam_viec (ngay_lam_viec, ma_bac_si, trang_thai)
SELECT d.date_val, b.ma_bac_si, 1
FROM (
  SELECT CURDATE() as date_val UNION ALL
  SELECT DATE_ADD(CURDATE(), INTERVAL 1 DAY) UNION ALL
  SELECT DATE_ADD(CURDATE(), INTERVAL 2 DAY) UNION ALL
  SELECT DATE_ADD(CURDATE(), INTERVAL 3 DAY) UNION ALL
  SELECT DATE_ADD(CURDATE(), INTERVAL 4 DAY) UNION ALL
  SELECT DATE_ADD(CURDATE(), INTERVAL 5 DAY) UNION ALL
  SELECT DATE_ADD(CURDATE(), INTERVAL 6 DAY)
) d
CROSS JOIN bac_si b;

-- 2. Ca khám theo ngày: lịch × khung giờ chuẩn đang bật
INSERT INTO khung_gio (ma_lich_lam_viec, ma_ca_kham, trang_thai)
SELECT l.ma_lich_lam_viec, c.ma_ca_kham, 1
FROM lich_lam_viec l
CROSS JOIN ca_kham_chuan c
WHERE c.trang_thai = 1;

-- 3. Trình độ
INSERT INTO trinh_do (ten_trinh_do, ngay_cap, ma_bac_si)
SELECT 'Bác sĩ Chuyên khoa Cấp II', '2012-06-15 00:00:00', ma_bac_si FROM bac_si
UNION ALL
SELECT 'Thạc sĩ Y khoa chuyên ngành Nội khoa', '2005-09-20 00:00:00', ma_bac_si FROM bac_si
UNION ALL
SELECT 'Bác sĩ Đa khoa - Đại học Y Hà Nội', '1998-06-10 00:00:00', ma_bac_si FROM bac_si;

-- 4. Đánh giá
INSERT INTO danh_gia_bac_si (binh_luan, danh_gia, ma_bac_si, ma_nguoi_dung)
SELECT 'Bác sĩ khám rất tận tâm, giải thích bệnh rõ ràng và kê đơn uống thuốc rất hiệu quả.', 5, ma_bac_si, 4 FROM bac_si
UNION ALL
SELECT 'Dịch vụ tốt, bác sĩ giỏi chuyên môn. Bệnh của tôi đã cải thiện đáng kể sau khi điều trị.', 5, ma_bac_si, 5 FROM bac_si
UNION ALL
SELECT 'Bác sĩ nhiệt tình, giải đáp mọi thắc mắc của người bệnh. Phòng khám sạch sẽ, thiết bị hiện đại.', 4, ma_bac_si, 6 FROM bac_si;

-- 5. Lịch sử giá
INSERT INTO lich_su_gia_kham (chi_phi_kham, ngay_ap_dung, trang_thai, ma_bac_si)
SELECT 150000.00, '2026-05-24 00:00:00', 1, ma_bac_si FROM bac_si;
