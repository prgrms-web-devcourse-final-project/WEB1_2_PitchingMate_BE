package com.example.mate.domain.goodsChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.common.util.validator.ValidPageable;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomSummaryResponse;
import com.example.mate.domain.goodsChat.service.GoodsChatService;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goods/chat")
@Tag(name = "GoodsChatRoomController", description = "굿즈거래 채팅방 관련 API")
public class GoodsChatRoomController {

    private final GoodsChatService goodsChatService;

    @PostMapping
    @Operation(summary = "굿즈거래 채팅방 입장 및 생성", description = "굿즈거래 게시글에 대한 채팅방을 생성하거나 기존 채팅방 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> createGoodsChatRoom(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "판매글 ID", required = true) @RequestParam Long goodsPostId
            ) {
        GoodsChatRoomResponse response = goodsChatService.getOrCreateGoodsChatRoom(member.getMemberId(), goodsPostId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/message")
    @Operation(summary = "굿즈거래 채팅방 메시지 조회", description = "지정된 채팅방의 메시지를 페이징 처리하여 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GoodsChatMessageResponse>>> getGoodsChatRoomMessages(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long chatRoomId,
            @Parameter(description = "페이징 정보") @ValidPageable(page = 1, size = 20) Pageable pageable
    ) {
        PageResponse<GoodsChatMessageResponse> response = goodsChatService.getChatRoomMessages(chatRoomId, member.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "사용자의 굿즈거래 채팅방 목록 조회", description = "사용자가 참여 중인 굿즈거래 채팅방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GoodsChatRoomSummaryResponse>>> getGoodsChatRooms(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "페이징 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<GoodsChatRoomSummaryResponse> response = goodsChatService.getGoodsChatRooms(member.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{chatRoomId}")
    @Operation(summary = "굿즈거래 채팅방 나가기", description = "사용자가 지정된 굿즈거래 채팅방을 나갑니다. 만약 모든 사용자가 나가면 채팅방이 삭제됩니다.")
    public ResponseEntity<Void> leaveGoodsChatRoom(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long chatRoomId
    ) {
        goodsChatService.deactivateGoodsChatPart(member.getMemberId(), chatRoomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatRoomId}")
    @Operation(summary = "굿즈거래 채팅방 입장", description = "굿즈거래 채팅방의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> getGoodsChatRoomInfo(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long chatRoomId
    ) {
        GoodsChatRoomResponse response = goodsChatService.getGoodsChatRoomInfo(member.getMemberId(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/members")
    @Operation(summary = "굿즈거래 채팅방 인원 조회", description = "지정된 채팅방에 참여 중인 사용자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberSummaryResponse>>> getGoodsChatRoomMembers(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long chatRoomId
    ) {
        List<MemberSummaryResponse> responses = goodsChatService.getMembersInChatRoom(member.getMemberId(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{chatRoomId}/complete")
    @Operation(summary = "굿즈 거래 완료", description = "굿즈거래 채팅방에서 굿즈거래를 거래완료 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> completeGoodsPost(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "채팅방 ID", required = true) @PathVariable Long chatRoomId
    ) {
        goodsChatService.completeTransaction(member.getMemberId(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
