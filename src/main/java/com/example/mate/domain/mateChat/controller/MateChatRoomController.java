package com.example.mate.domain.mateChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
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

    @PostMapping("/post/{matePostId}")
    @Operation(summary = "메이트 게시글 -> 채팅방 생성/입장", description = "메이트 게시글 페이지에서 채팅방으로 입장")
    public ResponseEntity<ApiResponse<MateChatRoomResponse>> createOrJoinChatRoomFromPost(
            @Parameter(description = "메이트 게시글 ID") @PathVariable Long matePostId,
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId
    ) {
        MateChatRoomResponse response = chatRoomService.createOrJoinChatRoomFromPost(matePostId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatroomId}/join")
    @Operation(summary = "채팅방 목록 -> 채팅방 입장", description = "채팅 목록 페이지에서 채팅방으로 입장")
    public ResponseEntity<ApiResponse<MateChatRoomResponse>> joinExistingChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long chatroomId,
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId
    ) {
        MateChatRoomResponse response = chatRoomService.joinExistingChatRoom(chatroomId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatroomId}/members/{memberId}/messages")
    @Operation(summary = "채팅방 메세지 조회", description = "채팅 목록 페이지 -> 채팅방 입장 시, 메시지 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MateChatMessageResponse>>> getChatMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long chatroomId,
            @PathVariable Long memberId,
            @Parameter(description = "페이지 정보") @PageableDefault Pageable pageable
    ) {
        PageResponse<MateChatMessageResponse> messages = chatRoomService.getChatMessages(chatroomId, memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/me")
    @Operation(summary = "내 채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MateChatRoomListResponse>>> getMyChatRooms(
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId,  // 추후 @AuthenticationPrincipal로 대체
            @Parameter(description = "페이지 정보") @PageableDefault Pageable pageable
    ) {
        PageResponse<MateChatRoomListResponse> response = chatRoomService.getMyChatRooms(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("{chatroomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 퇴장합니다.")
    public ResponseEntity<Void> leaveChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long chatroomId,
            @Parameter(description = "회원 ID (삭제 예정)") @RequestParam Long memberId  // 추후 @AuthenticationPrincipal로 대체
    ) {
        chatRoomService.leaveChatRoom(chatroomId, memberId);
        return ResponseEntity.noContent().build();
    }
}
