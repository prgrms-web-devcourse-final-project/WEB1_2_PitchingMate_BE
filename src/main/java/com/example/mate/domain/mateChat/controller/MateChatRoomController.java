package com.example.mate.domain.mateChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.service.MateChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates/chat")
public class MateChatRoomController {
    private final MateChatRoomService chatRoomService;

    // 1. 최초 채팅방 입장 시 호출 됨. 이후 handleEnter 메서드 호출로 인해 입장 메세지 전송됨
    // 2. 생성되어있던 채팅방에 새로운 유저가 입장 시 호출 됨. 이후 handleEnter 메서드 호출로 인해 입장 메세지 전송
    @PostMapping("/rooms/{matePostId}")
    public ResponseEntity<ApiResponse<MateChatRoomResponse>> createOrJoinChatRoom(
            @PathVariable Long matePostId,
            @RequestParam Long memberId  // 추후 @AuthenticationPrincipal로 대체
    ) {
        MateChatRoomResponse response = chatRoomService.joinChatRoom(matePostId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 기존에 들어갔었던 유저가 채팅방을 다시 들어갔을때 호출됨.
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<PageResponse<MateChatMessageResponse>>> getChatMessages(
            @PathVariable Long roomId,
            @PageableDefault Pageable pageable
    ) {
        PageResponse<MateChatMessageResponse> messages = chatRoomService.getChatMessages(roomId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    // 채팅 페이지를 눌렀을 때 채팅 목록을 반환하기 위해서 호출됨.
    @GetMapping("/rooms/me")
    public ResponseEntity<ApiResponse<PageResponse<MateChatRoomListResponse>>> getMyChatRooms(
            @RequestParam Long memberId,  // 추후 @AuthenticationPrincipal로 대체
            @PageableDefault Pageable pageable
    ) {
        PageResponse<MateChatRoomListResponse> response = chatRoomService.getMyChatRooms(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 채팅방 나갈 때 호출되고, 이후 handleLeave 메서드 호출로 인해 퇴장 메세지 전송됨
    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long roomId,
            @RequestParam Long memberId  // 추후 @AuthenticationPrincipal로 대체
    ) {
        chatRoomService.leaveChatRoom(roomId, memberId);
        return ResponseEntity.noContent().build();
    }
}
