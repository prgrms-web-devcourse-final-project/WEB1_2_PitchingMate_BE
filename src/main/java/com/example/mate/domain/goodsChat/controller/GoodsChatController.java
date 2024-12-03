package com.example.mate.domain.goodsChat.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMsgResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.service.GoodsChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goods/chat")
public class GoodsChatController {

    private final GoodsChatService goodsChatService;

    /*
    굿즈거래 상세 페이지 - 채팅방 입장
    TODO: @RequestParam Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/chat" 로 변경 예정
    */
    @PostMapping
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> createGoodsChatRoom(@RequestParam Long buyerId,
                                                                                  @RequestParam Long goodsPostId) {
        GoodsChatRoomResponse response = goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    굿즈거래 채팅방 페이지 - 채팅 내역 조회
    TODO: @RequestParam Long memberId -> @AuthenticationPrincipal 로 변경
    */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<PageResponse<GoodsChatMsgResponse>>> getGoodsChatRoomMessages(
            @PathVariable Long chatRoomId,
            @RequestParam Long memberId,
            @PageableDefault Pageable pageable
    ) {
        PageResponse<GoodsChatMsgResponse> response = goodsChatService.getMessagesForChatRoom(chatRoomId, memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
