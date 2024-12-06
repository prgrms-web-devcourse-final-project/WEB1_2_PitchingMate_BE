package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateChatMessageService {
    private final MateChatRoomRepository chatRoomRepository;
    private final MateChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @Transactional
    public void sendMessage(MateChatMessageRequest request) {
        MateChatRoom chatRoom = findChatRoomById(request.getRoomId());

        // 메시지 전송 가능 여부 검증
        if (!chatRoom.getIsMessageable()) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_MESSAGEABLE);
        }

        Member sender = findMemberById(request.getSenderId());
        MateChatMessage chatMessage = chatMessageRepository.save(MateChatMessageRequest.toEntity(chatRoom, request, sender));

        // 마지막 메시지 정보 업데이트
        chatRoom.updateLastChat(chatMessage.getContent());

        // 웹소켓으로 메시지 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/mate/" + request.getRoomId(),
                MateChatMessageResponse.of(chatMessage)
        );
    }

    @Transactional
    public void sendEnterMessage(MateChatMessageRequest request) {
        MateChatRoom chatRoom = findChatRoomById(request.getRoomId());
        Member sender = findMemberById(request.getSenderId());

        MateChatMessage chatMessage = chatMessageRepository.save(
                MateChatMessageRequest.toEntity(chatRoom, request, sender)
        );

        chatRoom.updateLastChat(chatMessage.getContent());

        messagingTemplate.convertAndSend(
                "/sub/chat/mate/" + request.getRoomId(),
                MateChatMessageResponse.of(chatMessage)
        );
    }

    @Transactional
    public void sendLeaveMessage(MateChatMessageRequest request) {
        MateChatRoom chatRoom = findChatRoomById(request.getRoomId());
        Member sender = findMemberById(request.getSenderId());

        MateChatMessage chatMessage = chatMessageRepository.save(
                MateChatMessageRequest.toEntity(chatRoom, request, sender)
        );

        chatRoom.updateLastChat(chatMessage.getContent());

        messagingTemplate.convertAndSend(
                "/sub/chat/mate/" + request.getRoomId(),
                MateChatMessageResponse.of(chatMessage)
        );
    }

    private MateChatRoom findChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }
}