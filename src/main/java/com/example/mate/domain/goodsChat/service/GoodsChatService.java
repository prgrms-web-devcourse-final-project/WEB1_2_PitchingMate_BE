package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMsgResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomSummaryResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsChatService {

    private final GoodsPostRepository goodsPostRepository;
    private final MemberRepository memberRepository;
    private final GoodsChatRoomRepository chatRoomRepository;
    private final GoodsChatPartRepository partRepository;
    private final GoodsChatMessageRepository messageRepository;

    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(Long buyerId, Long goodsPostId) {
        Member buyer = findMemberById(buyerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        Member seller = goodsPost.getSeller();

        validateCreateChatRoom(goodsPost, buyer, seller);

        // 구매자가 채팅방이 존재하면 기존 채팅방을 반환하고, 없다면 새로 생성하여 반환
        GoodsChatRoom goodsChatRoom = chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER)
                .orElseGet(() -> createChatRoom(goodsPost, buyer, seller));

        return GoodsChatRoomResponse.of(goodsChatRoom);
    }

    private void validateCreateChatRoom(GoodsPost goodsPost, Member seller, Member buyer) {
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_CHAT_CLOSED_POST);
        }
        if (seller == buyer) {
            throw new CustomException(ErrorCode.GOODS_CHAT_SELLER_CANNOT_START);
        }
    }

    private GoodsChatRoom createChatRoom(GoodsPost goodsPost, Member buyer, Member seller) {
        GoodsChatRoom goodsChatRoom = GoodsChatRoom.builder()
                .goodsPost(goodsPost)
                .build();
        
        GoodsChatRoom savedChatRoom = chatRoomRepository.save(goodsChatRoom);
        savedChatRoom.addChatParticipant(buyer, Role.BUYER);
        savedChatRoom.addChatParticipant(seller, Role.SELLER);

        return savedChatRoom;
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private GoodsPost findGoodsPostById(Long goodsPostId) {
        return goodsPostRepository.findById(goodsPostId).orElseThrow(() ->
                new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsChatMsgResponse> getMessagesForChatRoom(Long chatRoomId, Long memberId, Pageable pageable) {
        validateMemberParticipation(chatRoomId, memberId);
        Pageable validatePageable = PageResponse.validatePageable(pageable);

        Page<GoodsChatMessage> chatMessagePage = messageRepository.findByChatRoomId(chatRoomId, validatePageable);
        List<GoodsChatMsgResponse> content = chatMessagePage.getContent().stream()
                .map(GoodsChatMsgResponse::of)
                .toList();

        return PageResponse.from(chatMessagePage, content);
    }

    private void validateMemberParticipation(Long chatRoomId, Long memberId) {
        if (!partRepository.existsById(new GoodsChatPartId(chatRoomId, memberId))) {
            throw new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsChatRoomSummaryResponse> getGoodsChatRooms(Long memberId, Pageable pageable) {
        Member member = findMemberById(memberId);
        Pageable validatePageable = PageResponse.validatePageable(pageable);

        Page<GoodsChatRoom> chatRoomPage = chatRoomRepository.findChatRoomPageByMemberId(memberId, validatePageable);

        List<GoodsChatRoomSummaryResponse> content = chatRoomPage.getContent().stream()
                .map(chatRoom -> GoodsChatRoomSummaryResponse.of(chatRoom, getOpponentMember(chatRoom, member)))
                .toList();

        return PageResponse.from(chatRoomPage, content);
    }

    // 채팅 참여 테이블에서 상대방 회원 정보를 찾음
    private Member getOpponentMember(GoodsChatRoom chatRoom, Member member) {
        return chatRoom.getChatParts().stream()
                .filter(part -> part.getMember() != member)
                .findAny()
                .map(GoodsChatPart::getMember)
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_OPPONENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public GoodsChatRoomResponse getGoodsChatRoomInfo(Long memberId, Long chatRoomId) {
        validateMemberParticipation(chatRoomId, memberId);

        GoodsChatRoom goodsChatRoom = chatRoomRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND));
        return GoodsChatRoomResponse.of(goodsChatRoom);
    }
}
