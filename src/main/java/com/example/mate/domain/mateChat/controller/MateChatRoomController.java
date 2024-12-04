package com.example.mate.domain.mateChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.service.MateChatMessageService;
import com.example.mate.domain.mateChat.service.MateChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates/chat")
@Tag(name = "MateChatRoom", description = "메이트 채팅방 관련 API")
public class MateChatRoomController {
    private final MateChatRoomService chatRoomService;

    @PostMapping("/rooms/{matePostId}")
    public ResponseEntity<ApiResponse<MateChatRoomResponse>> createOrJoinChatRoom(
            @Parameter(description = "메이트 게시글 ID") @PathVariable Long matePostId,
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId  // 추후 @AuthenticationPrincipal로 대체
    ) {
        MateChatRoomResponse response = chatRoomService.createOrJoinChatRoom(matePostId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "채팅 메시지 조회", description = "채팅 목록 페이지에서 특정 채팅방 입장 시, 메시지 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MateChatMessageResponse>>> getChatMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Parameter(description = "페이지 정보") @PageableDefault Pageable pageable
    ) {
        PageResponse<MateChatMessageResponse> messages = chatRoomService.getChatMessages(roomId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    // 채팅 페이지를 눌렀을 때 채팅 목록을 반환하기 위해서 호출됨.
    @GetMapping("/rooms/me")
    @Operation(summary = "내 채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MateChatRoomListResponse>>> getMyChatRooms(
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId,  // 추후 @AuthenticationPrincipal로 대체
            @Parameter(description = "페이지 정보") @PageableDefault Pageable pageable
    ) {
        PageResponse<MateChatRoomListResponse> response = chatRoomService.getMyChatRooms(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 채팅방 나갈 때 호출되고, 이후 handleLeave 메서드 호출로 인해 퇴장 메세지 전송됨
    @DeleteMapping("/rooms/{roomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 퇴장합니다.")
    public ResponseEntity<Void> leaveChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId  // 추후 @AuthenticationPrincipal로 대체
    ) {
        chatRoomService.leaveChatRoom(roomId, memberId);
        return ResponseEntity.noContent().build();
    }
}
