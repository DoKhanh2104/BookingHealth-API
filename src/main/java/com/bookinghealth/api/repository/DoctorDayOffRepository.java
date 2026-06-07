package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.DoctorDayOff;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorDayOffRepository extends JpaRepository<DoctorDayOff, Long> {

  List<DoctorDayOff> findByDoctorIdOrderByIdDesc(Long doctorId);

  Page<DoctorDayOff> findAllByOrderByIdDesc(Pageable pageable);

  @Query("SELECT d FROM DoctorDayOff d WHERE d.status = :status ORDER BY d.id DESC")
  Page<DoctorDayOff> findByStatus(@Param("status") Integer status, Pageable pageable);
}
