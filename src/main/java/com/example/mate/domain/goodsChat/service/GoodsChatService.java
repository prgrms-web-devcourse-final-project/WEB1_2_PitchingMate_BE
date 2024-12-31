package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Role;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomSummaryResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.event.GoodsChatEvent;
import com.example.mate.domain.goodsChat.event.GoodsChatEventPublisher;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final GoodsChatEventPublisher eventPublisher;

    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(Long buyerId, Long goodsPostId) {
        Member buyer = findMemberById(buyerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        Member seller = goodsPost.getSeller();

        validateCreateChatRoom(goodsPost, buyer, seller);

        // 구매자가 채팅방이 존재하면 기존 채팅방을 반환하고, 없다면 새로 생성하여 반환
        return chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER)
                .map(this::buildChatRoomResponse)
                .orElseGet(() -> createChatRoom(goodsPost, buyer, seller));
    }

    // 기존 채팅방 & 채팅 내역 반환 (최신 20개)
    private GoodsChatRoomResponse buildChatRoomResponse(GoodsChatRoom chatRoom) {
        Page<GoodsChatMessage> messages = messageRepository.getChatMessages(chatRoom.getId(), PageRequest.of(0, 20));
        List<GoodsChatMessageResponse> content = messages.getContent().stream()
                .map(GoodsChatMessageResponse::of)
                .toList();
        return GoodsChatRoomResponse.of(chatRoom, PageResponse.from(messages, content));
    }

    // 새로운 채팅방 반환
    private GoodsChatRoomResponse createChatRoom(GoodsPost goodsPost, Member buyer, Member seller) {
        GoodsChatRoom goodsChatRoom = GoodsChatRoom.builder()
                .goodsPost(goodsPost)
                .build();

        GoodsChatRoom savedChatRoom = chatRoomRepository.save(goodsChatRoom);
        savedChatRoom.addChatParticipant(buyer, Role.BUYER);
        savedChatRoom.addChatParticipant(seller, Role.SELLER);

        // 새로운 채팅방 생성 - 입장 메시지 전송
        eventPublisher.publish(GoodsChatEvent.from(goodsChatRoom.getId(), buyer, MessageType.ENTER));

        return GoodsChatRoomResponse.of(savedChatRoom, null);
    }

    private void validateCreateChatRoom(GoodsPost goodsPost, Member seller, Member buyer) {
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_CHAT_CLOSED_POST);
        }
        if (seller == buyer) {
            throw new CustomException(ErrorCode.GOODS_CHAT_SELLER_CANNOT_START);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsChatMessageResponse> getMessagesForChatRoom(Long chatRoomId, Long memberId, Pageable pageable) {
        validateMemberParticipation(memberId, chatRoomId);
        Page<GoodsChatMessage> chatMessagePage = messageRepository.getChatMessages(chatRoomId, pageable);
        List<GoodsChatMessageResponse> content = chatMessagePage.getContent().stream()
                .map(GoodsChatMessageResponse::of)
                .toList();
        return PageResponse.from(chatMessagePage, content);
    }

    private void validateMemberParticipation(Long memberId, Long chatRoomId) {
        if (!partRepository.existsById(new GoodsChatPartId(memberId, chatRoomId))) {
            throw new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsChatRoomSummaryResponse> getGoodsChatRooms(Long memberId, Pageable pageable) {
        Member member = findMemberById(memberId);
        Page<GoodsChatRoom> chatRoomPage = chatRoomRepository.findChatRoomPageByMemberId(memberId, pageable);
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
        validateMemberParticipation(memberId, chatRoomId);
        GoodsChatRoom chatRoom = findChatRoomById(chatRoomId);

        Page<GoodsChatMessage> messages = messageRepository.getChatMessages(chatRoom.getId(), PageRequest.of(0, 20));
        List<GoodsChatMessageResponse> content = messages.getContent().stream()
                .map(GoodsChatMessageResponse::of)
                .toList();

        return GoodsChatRoomResponse.of(chatRoom, PageResponse.from(messages, content));
    }

    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getChatRoomMembers(Long memberId, Long chatRoomId) {
        validateMemberParticipation(memberId, chatRoomId);
        List<GoodsChatPart> goodsChatParts = partRepository.findAllWithMemberByChatRoomId(chatRoomId);

        return goodsChatParts.stream()
                .map(part -> MemberSummaryResponse.from(part.getMember()))
                .collect(Collectors.toList());
    }

    public void deactivateGoodsChatPart(Long memberId, Long chatRoomId) {
        Member member = findMemberById(memberId);
        GoodsChatPart goodsChatPart = partRepository.findById(new GoodsChatPartId(memberId, chatRoomId))
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART));

        if (!goodsChatPart.leaveAndCheckRoomStatus()) {
            // 퇴장 메시지 전송
            eventPublisher.publish(GoodsChatEvent.from(chatRoomId, member, MessageType.LEAVE));
        } else {
            // 모두 나갔다면 채팅방, 채팅 참여, 채팅 삭제
            chatRoomRepository.deleteById(chatRoomId);
        }
    }

    private GoodsChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private GoodsPost findGoodsPostById(Long goodsPostId) {
        return goodsPostRepository.findById(goodsPostId).orElseThrow(() ->
                new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));
    }
}
