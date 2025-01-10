package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import com.example.mate.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.event.GoodsChatEvent;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsChatMessageService {

    private final MemberRepository memberRepository;
    private final GoodsChatRoomRepository chatRoomRepository;
    private final GoodsChatPartRepository chatPartRepository;
    private final GoodsChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String MEMBER_ENTER_MESSAGE = "님이 대화를 시작했습니다.";
    private static final String MEMBER_LEAVE_MESSAGE = "님이 대화를 떠났습니다.";

    public void sendMessage(GoodsChatMessageRequest message) {
        Member sender = findMemberById(message.getSenderId());
        GoodsChatRoom chatRoom = findByChatRoomById(message.getRoomId());
        GoodsChatPart chatPart = findByChatPartById(sender.getId(), chatRoom.getId());

        // DB에 메시지 저장
        GoodsChatMessage chatMessage
                = messageRepository.save(createChatMessage(message.getMessage(), chatPart, message.getType()));
        chatRoom.updateLastChat(chatMessage.getContent(), chatMessage.getSentAt());

        GoodsChatMessageResponse response = GoodsChatMessageResponse.of(chatMessage, chatPart.getMember());
        sendToSubscribers(message.getRoomId(), response);
    }

    // 입장 및 퇴장 메시지 전송
    public void sendChatEventMessage(GoodsChatEvent event) {
        Member member = event.member();
        Long roomId = event.chatRoomId();

        GoodsChatRoom chatRoom = findByChatRoomById(roomId);
        GoodsChatPart chatPart = findByChatPartById(member.getId(), roomId);

        // 메시지 생성
        String message = member.getNickname();
        switch (event.type()) {
            case ENTER -> message += MEMBER_ENTER_MESSAGE;
            case LEAVE -> message += MEMBER_LEAVE_MESSAGE;
        }

        // Message DB에 저장
        GoodsChatMessage chatMessage = messageRepository.save(createChatMessage(message, chatPart, event.type()));
        chatRoom.updateLastChat(message, chatMessage.getSentAt());

        // 이벤트 메시지 전송
        sendToSubscribers(roomId, GoodsChatMessageResponse.of(chatMessage, chatPart.getMember()));
    }

    private GoodsChatMessage createChatMessage(String message, GoodsChatPart chatPart, MessageType type) {
        return GoodsChatMessage.builder()
                .chatRoomId(chatPart.getGoodsChatRoom().getId())
                .memberId(chatPart.getMember().getId())
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

    private GoodsChatPart findByChatPartById(Long memberId, Long chatRoomId) {
        return chatPartRepository.findById(new GoodsChatPartId(memberId, chatRoomId))
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART));
    }

    private void sendToSubscribers(Long roomId, GoodsChatMessageResponse message) {
        messagingTemplate.convertAndSend("/sub/chat/goods/" + roomId, message);
    }
}
