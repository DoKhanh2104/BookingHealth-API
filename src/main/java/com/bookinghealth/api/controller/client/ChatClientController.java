package com.bookinghealth.api.controller.client;

import com.bookinghealth.api.dto.response.ApiResponse;
import com.bookinghealth.api.dto.request.client.SendMessageRequest;
import com.bookinghealth.api.dto.response.client.ChatMessageResponse;
import com.bookinghealth.api.dto.response.client.ChatRoomResponse;
import com.bookinghealth.api.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat-rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatClientController {

  ChatService chatService;

  @GetMapping
  public ApiResponse<List<ChatRoomResponse>> getMyChatRooms() {
    return ApiResponse.<List<ChatRoomResponse>>builder()
        .result(chatService.getMyChatRooms())
        .build();
  }

  @GetMapping("/appointment/{appointmentId}")
  public ApiResponse<ChatRoomResponse> getRoomByAppointment(@PathVariable Long appointmentId) {
    return ApiResponse.<ChatRoomResponse>builder()
        .result(chatService.getOrCreateRoomByAppointment(appointmentId))
        .build();
  }

  @GetMapping("/{roomId}/messages")
  public ApiResponse<List<ChatMessageResponse>> getMessages(
      @PathVariable Long roomId,
      @PageableDefault(size = 100) Pageable pageable) {
    return ApiResponse.<List<ChatMessageResponse>>builder()
        .result(chatService.getMessages(roomId, pageable))
        .build();
  }

  @PostMapping("/{roomId}/messages")
  public ApiResponse<ChatMessageResponse> sendMessage(
      @PathVariable Long roomId,
      @RequestBody SendMessageRequest request) {
    return ApiResponse.<ChatMessageResponse>builder()
        .result(chatService.sendMessage(roomId, request))
        .build();
  }
}
