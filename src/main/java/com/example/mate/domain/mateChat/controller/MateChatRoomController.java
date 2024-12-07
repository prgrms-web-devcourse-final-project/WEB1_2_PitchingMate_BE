package com.example.mate.domain.mateChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.service.MateChatRoomService;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates/chat")
@Tag(name = "MateChatRoom", description = "메이트 채팅방 관련 API")
public class MateChatRoomController {
    private final MateChatRoomService chatRoomService;

    @PostMapping("/post/{matePostId}/join")
    @Operation(summary = "메이트 게시글 -> 채팅방 생성/입장", description = "메이트 게시글 페이지에서 채팅방으로 입장")
    public ResponseEntity<ApiResponse<MateChatRoomResponse>> createOrJoinChatRoomFromPost(
            @Parameter(description = "메이트 게시글 ID") @PathVariable Long matePostId,
                                                        @AuthenticationPrincipal AuthMember member
    ) {
        MateChatRoomResponse response = chatRoomService.createOrJoinChatRoomFromPost(matePostId, member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MateChatRoomListResponse>>> getMyChatRooms(
            @Parameter(description = "페이지 정보") @PageableDefault Pageable pageable,
                                                    @AuthenticationPrincipal AuthMember member

            ) {
        PageResponse<MateChatRoomListResponse> response = chatRoomService.getMyChatRooms(member.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatroomId}/join")
    @Operation(summary = "채팅방 목록 -> 채팅방 입장", description = "채팅 목록 페이지에서 채팅방으로 입장")
    public ResponseEntity<ApiResponse<MateChatRoomResponse>> joinExistingChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long chatroomId,
            @AuthenticationPrincipal AuthMember member
    ) {
        MateChatRoomResponse response = chatRoomService.joinExistingChatRoom(chatroomId, member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatroomId}/messages")
    @Operation(summary = "채팅방 메세지 조회", description = "메시지 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MateChatMessageResponse>>> getChatMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long chatroomId,
                                                    @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "페이지 정보") @PageableDefault(page = 1, size = 20) Pageable pageable
    ) {
        PageResponse<MateChatMessageResponse> messages = chatRoomService.getChatMessages(chatroomId, member.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @DeleteMapping("{chatroomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 퇴장합니다.")
    public ResponseEntity<Void> leaveChatRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long chatroomId,
                                                    @AuthenticationPrincipal AuthMember member
    ) {
        chatRoomService.leaveChatRoom(chatroomId, member.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatRoomId}/members")
    @Operation(summary = "채팅방 멤버 조회", description = "채팅방에 현재 참여 중인 멤버 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberSummaryResponse>>> getMateChatRoomMembers(@Parameter(description = "채팅방 ID") @PathVariable Long chatRoomId,
                                                                                             @AuthenticationPrincipal AuthMember member) {
        List<MemberSummaryResponse> responses = chatRoomService.getChatRoomMembers(chatRoomId, member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
