package com.bookinghealth.api.repository;

import com.bookinghealth.api.dto.response.admin.DoctorAdminResponse;
import com.bookinghealth.api.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

  @Query(
      "SELECT new com.bookinghealth.api.dto.response.admin.DoctorAdminResponse("
          + "d.id, u.name, u.email, u.phone, c.clinicName, d.practiceLicenseNumber, d.status, s.specialtyName, d.practiceStartDate, u.avatar) "
          + "FROM Doctor d "
          + "JOIN d.user u "
          + "LEFT JOIN d.clinic c "
          + "LEFT JOIN d.specialties s "
          + "WHERE (:search IS NULL OR u.name LIKE %:search% OR d.practiceLicenseNumber LIKE %:search%) "
          + "AND (:status IS NULL OR d.status = :status)")
  Page<DoctorAdminResponse> searchDoctorsForAdmin(
      @Param("search") String search, @Param("status") Integer status, Pageable pageable);

  boolean existsByPracticeLicenseNumber(String practiceLicenseNumber);

  @Query(
      "SELECT DISTINCT d FROM Doctor d "
          + "JOIN d.user u "
          + "LEFT JOIN d.specialties s "
          + "WHERE d.status = 1 "
          + "AND (:specialtyId IS NULL OR s.id = :specialtyId) "
          + "AND (:clinicId IS NULL OR d.clinic.id = :clinicId) "
          + "AND (:search IS NULL OR u.name LIKE %:search%)")
  Page<Doctor> searchDoctorsForClient(
      @Param("specialtyId") Long specialtyId,
      @Param("clinicId") Long clinicId,
      @Param("search") String search,
      Pageable pageable);
}
