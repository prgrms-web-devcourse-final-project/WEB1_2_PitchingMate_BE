package com.example.mate.domain.goodsChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomSummaryResponse;
import com.example.mate.domain.goodsChat.service.GoodsChatService;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
public class GoodsChatRoomController {

    private final GoodsChatService goodsChatService;

    @PostMapping
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> createGoodsChatRoom(@AuthenticationPrincipal AuthMember member,
                                                                                  @RequestParam Long goodsPostId) {
        GoodsChatRoomResponse response = goodsChatService.getOrCreateGoodsChatRoom(member.getMemberId(), goodsPostId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/message")
    public ResponseEntity<ApiResponse<PageResponse<GoodsChatMessageResponse>>> getGoodsChatRoomMessages(
            @AuthenticationPrincipal AuthMember member,
            @PathVariable Long chatRoomId,
            @PageableDefault(page = 1, size = 20) Pageable pageable
    ) {
        PageResponse<GoodsChatMessageResponse> response = goodsChatService.getMessagesForChatRoom(chatRoomId, member.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GoodsChatRoomSummaryResponse>>> getGoodsChatRooms(@AuthenticationPrincipal AuthMember member,
                                                                                                     @PageableDefault Pageable pageable) {
        PageResponse<GoodsChatRoomSummaryResponse> response = goodsChatService.getGoodsChatRooms(member.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> leaveGoodsChatRoom(@AuthenticationPrincipal AuthMember member, @PathVariable Long chatRoomId) {
        goodsChatService.deactivateGoodsChatPart(member.getMemberId(), chatRoomId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> getGoodsChatRoomInfo(@AuthenticationPrincipal AuthMember member,
                                                                                   @PathVariable Long chatRoomId) {
        GoodsChatRoomResponse response = goodsChatService.getGoodsChatRoomInfo(member.getMemberId(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/members")
    public ResponseEntity<ApiResponse<List<MemberSummaryResponse>>> getGoodsChatRoomMembers(@AuthenticationPrincipal AuthMember member,
                                                                                            @PathVariable Long chatRoomId) {
        List<MemberSummaryResponse> responses = goodsChatService.getChatRoomMembers(member.getMemberId(), chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
