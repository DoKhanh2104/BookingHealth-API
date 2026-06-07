package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  Optional<ChatRoom> findByAppointmentId(Long appointmentId);

  List<ChatRoom> findByUserId(Long userId);

  List<ChatRoom> findByDoctorId(Long doctorId);
}
