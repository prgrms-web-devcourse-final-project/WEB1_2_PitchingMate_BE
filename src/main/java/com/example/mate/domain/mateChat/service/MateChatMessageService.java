package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.mateChat.document.MateChatMessage;
import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.event.MateChatEvent;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.mate.common.error.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateChatMessageService {
    private final MateChatRoomRepository chatRoomRepository;
    private final MateChatMessageRepository chatMessageRepository; // 새로운 MongoDB 레포지토리
    private final MemberRepository memberRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    public void sendMessage(MateChatMessageRequest request) {
        MateChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));
        if (!chatRoom.getIsMessageable()) {
            throw new CustomException(CHAT_ROOM_NOT_MESSAGEABLE);
        }

        Member sender = memberRepository.findById(request.getSenderId())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND_BY_ID));

        // MongoDB에 메시지 저장
        MateChatMessage message = MateChatMessage.builder()
                .roomId(chatRoom.getId())
                .senderId(sender.getId())
                .content(request.getMessage())
                .type(MessageType.TALK)
                .sendTime(LocalDateTime.now())
                .build();

        MateChatMessage savedMessage = chatMessageRepository.save(message);

        // 마지막 메시지 정보 업데이트
        chatRoom.updateLastChat(request.getMessage());

        // 웹소켓으로 메시지 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/mate/" + request.getRoomId(),
                createMessageResponse(savedMessage, sender)
        );
    }

    @Transactional
    public void sendChatEventMessage(MateChatEvent event) {
        MateChatRoom chatRoom = chatRoomRepository.findById(event.chatRoomId())
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));

        Member sender = event.member();

        String eventType = event.type() == MessageType.ENTER ?
                "님이 입장하셨습니다." : "님이 퇴장하셨습니다.";
        String eventMessage = sender.getNickname() + eventType;

        // 이벤트 메시지 저장
        MateChatMessage message = MateChatMessage.builder()
                .roomId(chatRoom.getId())
                .senderId(sender.getId())
                .content(eventMessage)
                .type(event.type())
                .sendTime(LocalDateTime.now())
                .build();

        MateChatMessage savedMessage = chatMessageRepository.save(message);

        // 채팅방 마지막 메시지 업데이트
        chatRoom.updateLastChat(eventMessage);

        messagingTemplate.convertAndSend(
                "/sub/chat/mate/" + event.chatRoomId(),
                createMessageResponse(savedMessage, sender)
        );
    }

    private MateChatMessageResponse createMessageResponse(MateChatMessage message, Member sender) {
        return MateChatMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .senderImageUrl(sender.getImageUrl())
                .message(message.getContent())
                .messageType(message.getType().getValue())
                .sendTime(message.getSendTime())
                .build();
    }
}