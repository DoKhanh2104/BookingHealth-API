-- Migration: tách khung giờ chuẩn (admin) và ca khám theo ngày (bác sĩ)
-- Chạy: mysql -h 127.0.0.1 -P 3307 -u root -proot bookinghealth < migrate_time_slots.sql

CREATE TABLE IF NOT EXISTS ca_kham_chuan (
  ma_ca_kham BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  thoi_gian_bat_dau TIME DEFAULT NULL,
  thoi_gian_ket_thuc TIME DEFAULT NULL,
  trang_thai INT DEFAULT 1
);

INSERT INTO ca_kham_chuan (thoi_gian_bat_dau, thoi_gian_ket_thuc, trang_thai)
SELECT DISTINCT kg.thoi_gian_bat_dau, kg.thoi_gian_ket_thuc, 1
FROM khung_gio kg
WHERE kg.thoi_gian_bat_dau IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM ca_kham_chuan c
    WHERE c.thoi_gian_bat_dau = kg.thoi_gian_bat_dau
      AND c.thoi_gian_ket_thuc = kg.thoi_gian_ket_thuc
  );

SET @has_ma_ca_kham = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'khung_gio'
    AND COLUMN_NAME = 'ma_ca_kham'
);

SET @sql_add_col = IF(
  @has_ma_ca_kham = 0,
  'ALTER TABLE khung_gio ADD COLUMN ma_ca_kham BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql_add_col;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE khung_gio kg
INNER JOIN ca_kham_chuan c
  ON c.thoi_gian_bat_dau = kg.thoi_gian_bat_dau
 AND c.thoi_gian_ket_thuc = kg.thoi_gian_ket_thuc
SET kg.ma_ca_kham = c.ma_ca_kham
WHERE kg.ma_ca_kham IS NULL;

DELETE FROM khung_gio WHERE ma_ca_kham IS NULL;

DELETE kg1 FROM khung_gio kg1
INNER JOIN khung_gio kg2
  ON kg1.ma_lich_lam_viec = kg2.ma_lich_lam_viec
 AND kg1.ma_ca_kham = kg2.ma_ca_kham
 AND kg1.ma_khung_gio > kg2.ma_khung_gio;

SET @has_start = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'khung_gio'
    AND COLUMN_NAME = 'thoi_gian_bat_dau'
);
SET @sql_drop_time = IF(
  @has_start > 0,
  'ALTER TABLE khung_gio DROP COLUMN thoi_gian_bat_dau, DROP COLUMN thoi_gian_ket_thuc',
  'SELECT 1'
);
PREPARE stmt2 FROM @sql_drop_time;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

ALTER TABLE khung_gio MODIFY ma_ca_kham BIGINT NOT NULL;

SET @has_uk = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'khung_gio'
    AND CONSTRAINT_NAME = 'uk_lich_ca'
);
SET @sql_uk = IF(
  @has_uk = 0,
  'ALTER TABLE khung_gio ADD UNIQUE KEY uk_lich_ca (ma_lich_lam_viec, ma_ca_kham)',
  'SELECT 1'
);
PREPARE stmt3 FROM @sql_uk;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;
