package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomSummaryResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.event.GoodsChatEvent;
import com.example.mate.domain.goodsChat.event.GoodsChatEventPublisher;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Role;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.event.GoodsPostEvent;
import com.example.mate.domain.goodsPost.event.GoodsPostEventPublisher;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.ActivityType;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
    private final GoodsChatEventPublisher chatEventPublisher;
    private final GoodsPostEventPublisher notificationEventPublisher;

    // 채팅방 생성 & 기존 채팅방 입장
    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(Long buyerId, Long goodsPostId) {
        // 구매자, 판매글, 판매자 조회 및 검증
        Member buyer = findMemberById(buyerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        Member seller = goodsPost.getSeller();

        // 채팅방 생성 유효성 검증
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_CHAT_CLOSED_POST);
        }
        if (seller == buyer) {
            throw new CustomException(ErrorCode.GOODS_CHAT_SELLER_CANNOT_START);
        }

        // 기존 채팅방이 있으면 반환, 없으면 새 채팅방 생성
        return chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER)
                .map(this::getChatRoomWithMessages)
                .orElseGet(() -> createChatRoom(goodsPost, buyer, seller));
    }

    // 채팅방 정보와 채팅 내역 반환
    private GoodsChatRoomResponse getChatRoomWithMessages(GoodsChatRoom chatRoom) {
        List<GoodsChatMessage> chatMessages = messageRepository.getChatMessages(chatRoom.getId(), null, 20);
        List<GoodsChatMessageResponse> chatMessageResponses = mapMessagesToResponses(chatMessages);

        return GoodsChatRoomResponse.of(chatRoom, chatMessageResponses);
    }

    // 메시지 발신자 정보 조회 및 DTO 매핑
    private List<GoodsChatMessageResponse> mapMessagesToResponses(List<GoodsChatMessage> chatMessages) {
        return chatMessages.stream()
                .map(message -> {
                    Long memberId = message.getMemberId();
                    Member member = findMemberById(memberId);
                    return GoodsChatMessageResponse.of(message, member);
                })
                .toList();
    }

    // 새 채팅방 생성
    private GoodsChatRoomResponse createChatRoom(GoodsPost goodsPost, Member buyer, Member seller) {
        GoodsChatRoom goodsChatRoom = GoodsChatRoom.builder()
                .goodsPost(goodsPost)
                .build();

        GoodsChatRoom savedChatRoom = chatRoomRepository.save(goodsChatRoom);
        savedChatRoom.addChatParticipant(buyer, Role.BUYER);
        savedChatRoom.addChatParticipant(seller, Role.SELLER);

        // 입장 메시지 이벤트 전송
        chatEventPublisher.publish(GoodsChatEvent.from(goodsChatRoom.getId(), buyer, MessageType.ENTER));

        return GoodsChatRoomResponse.of(savedChatRoom, null);
    }

    // 단순 채팅 내역 조회
    @Transactional(readOnly = true)
    public List<GoodsChatMessageResponse> getChatRoomMessages(Long chatRoomId, Long memberId, LocalDateTime lastSentAt) {
        validateMemberInChatRoom(memberId, chatRoomId);
        List<GoodsChatMessage> chatMessages = messageRepository.getChatMessages(chatRoomId, lastSentAt, 20);

        return mapMessagesToResponses(chatMessages);
    }

    private void validateMemberInChatRoom(Long memberId, Long chatRoomId) {
        if (!partRepository.existsById(new GoodsChatPartId(memberId, chatRoomId))) {
            throw new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART);
        }
    }

    // 채팅 목록 조회
    @Transactional(readOnly = true)
    public PageResponse<GoodsChatRoomSummaryResponse> getGoodsChatRooms(Long memberId, Pageable pageable) {
        Member member = findMemberById(memberId);
        Page<GoodsChatRoom> chatRoomPage = chatRoomRepository.findChatRoomPageByMemberId(memberId, pageable);

        List<GoodsChatRoomSummaryResponse> summaries = chatRoomPage.getContent().stream()
                .map(chatRoom -> GoodsChatRoomSummaryResponse.of(chatRoom, getOpponentMember(chatRoom, member)))
                .toList();

        return PageResponse.from(chatRoomPage, summaries);
    }

    // 상대방 회원 조회
    private Member getOpponentMember(GoodsChatRoom chatRoom, Member currentUser) {
        return chatRoom.getChatParts().stream()
                .map(GoodsChatPart::getMember)
                .filter(member -> !member.equals(currentUser))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_OPPONENT_NOT_FOUND));
    }

    // 채팅방 입장
    @Transactional(readOnly = true)
    public GoodsChatRoomResponse getGoodsChatRoomInfo(Long memberId, Long chatRoomId) {
        validateMemberInChatRoom(memberId, chatRoomId);
        GoodsChatRoom chatRoom = findChatRoomById(chatRoomId);

        return getChatRoomWithMessages(chatRoom);
    }

    // 채팅방 참여 인원 조회
    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getMembersInChatRoom(Long memberId, Long chatRoomId) {
        validateMemberInChatRoom(memberId, chatRoomId);
        List<GoodsChatPart> goodsChatParts = partRepository.findAllWithMemberByChatRoomId(chatRoomId);

        return goodsChatParts.stream()
                .map(part -> MemberSummaryResponse.from(part.getMember()))
                .collect(Collectors.toList());
    }

    // 채팅방 나가기 & 채팅 정보 삭제
    public void deactivateGoodsChatPart(Long memberId, Long chatRoomId) {
        Member member = findMemberById(memberId);
        GoodsChatPart goodsChatPart = partRepository.findById(new GoodsChatPartId(memberId, chatRoomId))
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART));

        if (!goodsChatPart.leaveAndCheckRoomStatus()) {
            // 퇴장 메시지 전송
            chatEventPublisher.publish(GoodsChatEvent.from(chatRoomId, member, MessageType.LEAVE));
        } else {
            // 모두 나갔다면 채팅방, 채팅 참여, 채팅 삭제
            deleteChatRoom(chatRoomId);
        }
    }

    // 굿즈 거래완료
    public void completeTransaction(Long sellerId, Long chatRoomId) {
        Member seller = findMemberById(sellerId);
        GoodsChatRoom chatRoom = findChatRoomById(chatRoomId);
        Member buyer = getOpponentMember(chatRoom, seller);
        GoodsPost goodsPost = chatRoom.getGoodsPost();

        if (!goodsPost.getSeller().equals(seller)) {
            throw new CustomException(ErrorCode.GOODS_MODIFICATION_NOT_ALLOWED);
        }
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_ALREADY_COMPLETED);
        }

        goodsPost.completeTransaction(buyer);
        seller.updateManner(ActivityType.GOODS);
        buyer.updateManner(ActivityType.GOODS);

        // 거래완료 알림 및 채팅 메시지 전송
        chatEventPublisher.publish(GoodsChatEvent.from(chatRoomId, seller, MessageType.GOODS));
        notificationEventPublisher.publish(GoodsPostEvent.of(goodsPost.getId(), goodsPost.getTitle(), buyer, NotificationType.GOODS_CLOSED));
    }

    // 채팅방 삭제
    private void deleteChatRoom(Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId);
        messageRepository.deleteAllByChatRoomId(chatRoomId); // 메시지 삭제
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
