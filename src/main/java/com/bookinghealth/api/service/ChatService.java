package com.bookinghealth.api.service;

import com.bookinghealth.api.dto.request.client.SendMessageRequest;
import com.bookinghealth.api.dto.response.client.ChatMessageResponse;
import com.bookinghealth.api.dto.response.client.ChatRoomResponse;
import com.bookinghealth.api.entity.*;
import com.bookinghealth.api.exception.AppException;
import com.bookinghealth.api.exception.ErrorCode;
import com.bookinghealth.api.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {

  ChatRoomRepository chatRoomRepository;
  MessageRepository messageRepository;
  AppointmentRepository appointmentRepository;
  UserRepository userRepository;

  @Transactional
  public ChatRoomResponse getOrCreateRoomByAppointment(Long appointmentId) {
    User currentUser = getCurrentUser();

    Appointment appointment = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

    // Verify appointment status is completed (2)
    if (appointment.getStatus() == null || appointment.getStatus() != 2) {
      throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
    }

    // Verify currentUser is either the patient or the doctor for this appointment
    boolean isPatient = appointment.getUser() != null && appointment.getUser().getId().equals(currentUser.getId());
    boolean isDoctor = appointment.getDoctor() != null && appointment.getDoctor().getUser() != null && 
                       appointment.getDoctor().getUser().getId().equals(currentUser.getId());

    if (!isPatient && !isDoctor) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    ChatRoom chatRoom = chatRoomRepository.findByAppointmentId(appointmentId)
        .orElseGet(() -> {
          ChatRoom newRoom = ChatRoom.builder()
              .appointment(appointment)
              .user(appointment.getUser())
              .doctor(appointment.getDoctor())
              .status(1) // 1: Open
              .build();
          return chatRoomRepository.save(newRoom);
        });

    return mapToChatRoomResponse(chatRoom);
  }

  public List<ChatRoomResponse> getMyChatRooms() {
    User currentUser = getCurrentUser();

    List<ChatRoom> rooms;
    if (currentUser.getDoctor() != null) {
      rooms = chatRoomRepository.findByDoctorId(currentUser.getDoctor().getId());
    } else {
      rooms = chatRoomRepository.findByUserId(currentUser.getId());
    }

    return rooms.stream()
        .map(this::mapToChatRoomResponse)
        .sorted((r1, r2) -> {
          if (r1.getLastMessageTime() == null && r2.getLastMessageTime() == null) {
            return r2.getId().compareTo(r1.getId());
          }
          if (r1.getLastMessageTime() == null || r1.getLastMessageTime().isEmpty()) return 1;
          if (r2.getLastMessageTime() == null || r2.getLastMessageTime().isEmpty()) return -1;
          return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
        })
        .collect(Collectors.toList());
  }

  public List<ChatMessageResponse> getMessages(Long roomId, Pageable pageable) {
    User currentUser = getCurrentUser();

    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION)); // Room not found

    // Verify member of room
    boolean isPatient = chatRoom.getUser() != null && chatRoom.getUser().getId().equals(currentUser.getId());
    boolean isDoctor = chatRoom.getDoctor() != null && chatRoom.getDoctor().getUser() != null && 
                       chatRoom.getDoctor().getUser().getId().equals(currentUser.getId());

    if (!isPatient && !isDoctor) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    return messageRepository.findByChatRoomIdOrderBySendTimeAsc(roomId, pageable)
        .stream()
        .map(this::mapToChatMessageResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public ChatMessageResponse sendMessage(Long roomId, SendMessageRequest request) {
    User currentUser = getCurrentUser();

    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION)); // Room not found

    // Verify room is open
    if (chatRoom.getStatus() == null || chatRoom.getStatus() != 1) {
      throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    // Verify member
    boolean isPatient = chatRoom.getUser() != null && chatRoom.getUser().getId().equals(currentUser.getId());
    boolean isDoctor = chatRoom.getDoctor() != null && chatRoom.getDoctor().getUser() != null && 
                       chatRoom.getDoctor().getUser().getId().equals(currentUser.getId());

    if (!isPatient && !isDoctor) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    Message message = Message.builder()
        .chatRoom(chatRoom)
        .sender(currentUser)
        .content(request.getContent())
        .sendTime(LocalDateTime.now())
        .build();

    Message saved = messageRepository.save(message);
    return mapToChatMessageResponse(saved);
  }

  private ChatRoomResponse mapToChatRoomResponse(ChatRoom room) {
    Message lastMsg = messageRepository.findFirstByChatRoomIdOrderBySendTimeDesc(room.getId()).orElse(null);

    String lastContent = lastMsg != null ? lastMsg.getContent() : "";
    String lastTime = lastMsg != null ? lastMsg.getSendTime().toString() : "";

    return ChatRoomResponse.builder()
        .id(room.getId())
        .appointmentId(room.getAppointment() != null ? room.getAppointment().getId() : null)
        .doctorId(room.getDoctor() != null ? room.getDoctor().getId() : null)
        .doctorName(room.getDoctor() != null && room.getDoctor().getUser() != null ? room.getDoctor().getUser().getName() : "")
        .doctorAvatar(room.getDoctor() != null && room.getDoctor().getUser() != null ? room.getDoctor().getUser().getAvatar() : "")
        .userId(room.getUser() != null ? room.getUser().getId() : null)
        .userName(room.getUser() != null ? room.getUser().getName() : "")
        .userAvatar(room.getUser() != null ? room.getUser().getAvatar() : "")
        .status(room.getStatus())
        .lastMessage(lastContent)
        .lastMessageTime(lastTime)
        .build();
  }

  private ChatMessageResponse mapToChatMessageResponse(Message msg) {
    return ChatMessageResponse.builder()
        .id(msg.getId())
        .chatRoomId(msg.getChatRoom().getId())
        .senderId(msg.getSender().getId())
        .senderName(msg.getSender().getName())
        .senderAvatar(msg.getSender().getAvatar())
        .content(msg.getContent())
        .sendTime(msg.getSendTime().toString())
        .build();
  }

  private User getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    String identifier = authentication.getName();
    return userRepository.findByEmail(identifier)
        .or(() -> userRepository.findByPhone(identifier))
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
  }
}
