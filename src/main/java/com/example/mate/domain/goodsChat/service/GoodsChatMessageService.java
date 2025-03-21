package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import com.example.mate.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.event.GoodsChatEvent;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsChatMessageService {

    private final MemberRepository memberRepository;
    private final GoodsChatRoomRepository chatRoomRepository;
    private final GoodsChatMessageRepository messageRepository;
    private final GoodsChatCacheManager goodsChatCacheManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate mongoTransactionTemplate;

    private static final String GOODS_CHAT_SUBSCRIBE_PATH = "/sub/chat/goods/";
    private static final String MEMBER_ENTER_MESSAGE = "님이 대화를 시작했습니다.";
    private static final String MEMBER_LEAVE_MESSAGE = "님이 대화를 떠났습니다.";
    private static final String MEMBER_TRANSACTION_MESSAGE = "님이 거래를 완료했습니다. 상품에 대한 거래후기를 남겨주세요!";

    // 채팅 메시지 전송
    public void sendMessage(GoodsChatMessageRequest message) {
        Member member = findMemberById(message.getSenderId());
        GoodsChatRoom chatRoom = findByChatRoomById(message.getRoomId());
        saveAndSendMessage(chatRoom, member, message.getMessage(), message.getType());
    }

    // 이벤트 메시지 전송
    public void sendChatEventMessage(GoodsChatEvent event) {
        Member member = event.member();
        Long chatRoomId = event.chatRoomId();
        GoodsChatRoom chatRoom = findByChatRoomById(chatRoomId);

        // 메시지 생성
        String message = member.getNickname();
        switch (event.type()) {
            case ENTER -> message += MEMBER_ENTER_MESSAGE;
            case LEAVE -> message += MEMBER_LEAVE_MESSAGE;
            case GOODS -> message += MEMBER_TRANSACTION_MESSAGE;
        }

        saveAndSendMessage(chatRoom, member, message, event.type());
    }

    // 메시지 저장 및 전송
    private void saveAndSendMessage(GoodsChatRoom chatRoom, Member member, String message, MessageType type) {
        // 채팅 도큐먼트 생성
        GoodsChatMessage chatMessage = createChatMessage(chatRoom.getId(), member.getId(), message, type);

        // 최신 채팅 내역 업데이트
        chatRoom.updateLastChat(message, chatMessage.getSentAt());

        // MongoDB 트랜잭션
        mongoTransactionTemplate.executeWithoutResult(status -> {
            // 채팅 메시지 저장
            GoodsChatMessage savedMessage = messageRepository.save(chatMessage);

            // redis 캐시 저장
            goodsChatCacheManager.storeMessageInCache(savedMessage.getChatRoomId(), savedMessage);

            // 메시지 전송
            sendToSubscribers(savedMessage.getChatRoomId(), GoodsChatMessageResponse.of(savedMessage, member));
        });
    }

    private GoodsChatMessage createChatMessage(Long chatRoomId, Long memberId, String message, MessageType type) {
        return GoodsChatMessage.builder()
                .chatRoomId(chatRoomId)
                .memberId(memberId)
                .sentAt(LocalDateTime.now())
                .content(message)
                .messageType(type)
                .build();
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private GoodsChatRoom findByChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND));
    }

    private void sendToSubscribers(Long chatRoomId, GoodsChatMessageResponse message) {
        messagingTemplate.convertAndSend(GOODS_CHAT_SUBSCRIBE_PATH + chatRoomId, message);
    }
}
