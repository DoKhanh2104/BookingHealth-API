package com.bookinghealth.api.repository;

import com.bookinghealth.api.entity.Message;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  Page<Message> findByChatRoomIdOrderBySendTimeAsc(Long chatRoomId, Pageable pageable);

  Optional<Message> findFirstByChatRoomIdOrderBySendTimeDesc(Long chatRoomId);
}
