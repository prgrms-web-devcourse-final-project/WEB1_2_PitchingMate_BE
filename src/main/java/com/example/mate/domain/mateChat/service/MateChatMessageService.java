package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MateChatMessageService {
    private final MateChatRoomRepository chatRoomRepository;
    private final MateChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    // 일반 메시지 처리
    public void sendMessage(MateChatMessageRequest message) {
        Member sender = findMemberById(message.getSenderId());
        MateChatRoom chatRoom = findById(message.getRoomId());

        // DB에 메시지 저장
        MateChatMessage chatMessage = MateChatMessage.builder()
                .type(message.getType())
                .mateChatRoom(chatRoom)
                .sender(sender)
                .content(message.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 마지막 메시지 정보 업데이트
        chatRoom.updateLastChat(message.getMessage());
        chatRoomRepository.save(chatRoom);

        MateChatMessageResponse response = MateChatMessageResponse.of(
                message,
                sender.getNickname(),
                chatRoom.getCurrentMembers()
        );

        sendToSubscribers(message.getRoomId(), response);
    }

    // 입장 메시지 처리
    public void sendEnterMessage(MateChatMessageRequest message) {
        Member member = findMemberById(message.getSenderId());
        MateChatRoom chatRoom = findById(message.getRoomId());

        String enterMessage = member.getNickname() + "님이 입장하셨습니다.";
        MateChatMessageRequest enterRequest = createSystemMessage(
                MessageType.ENTER,
                message.getRoomId(),
                message.getSenderId(),
                enterMessage
        );

        // DB에 메시지 저장
        MateChatMessage chatMessage = MateChatMessage.builder()
                .type(message.getType())
                .mateChatRoom(chatRoom)
                .sender(member)
                .content(enterMessage)
                .build();
        chatMessageRepository.save(chatMessage);

        // 마지막 메시지 정보 업데이트
        chatRoom.updateLastChat(enterMessage);
        chatRoomRepository.save(chatRoom);

        MateChatMessageResponse response = MateChatMessageResponse.of(
                enterRequest,
                member.getNickname(),
                chatRoom.getCurrentMembers()
        );

        sendToSubscribers(message.getRoomId(), response);
    }

    // 퇴장 메시지 처리
    public void sendLeaveMessage(MateChatMessageRequest message) {
        Member member = findMemberById(message.getSenderId());
        MateChatRoom chatRoom = findById(message.getRoomId());

        String leaveMessage = member.getNickname() + "님이 퇴장하셨습니다.";
        MateChatMessageRequest leaveRequest = createSystemMessage(
                MessageType.LEAVE,
                message.getRoomId(),
                message.getSenderId(),
                leaveMessage
        );

        // DB에 메시지 저장
        MateChatMessage chatMessage = MateChatMessage.builder()
                .type(message.getType())
                .mateChatRoom(chatRoom)
                .sender(member)
                .content(leaveMessage)
                .build();
        chatMessageRepository.save(chatMessage);

        // 마지막 메시지 정보 업데이트
        chatRoom.updateLastChat(leaveMessage);
        chatRoomRepository.save(chatRoom);

        MateChatMessageResponse response = MateChatMessageResponse.of(
                leaveRequest,
                member.getNickname(),
                chatRoom.getCurrentMembers() - 1
        );

        sendToSubscribers(message.getRoomId(), response);
    }

    // 유틸리티 메서드
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private MateChatMessageRequest createSystemMessage(
            MessageType type, Long roomId, Long senderId, String message) {
        return MateChatMessageRequest.builder()
                .type(type)
                .roomId(roomId)
                .senderId(senderId)
                .message(message)
                .build();
    }

    private void sendToSubscribers(Long roomId, MateChatMessageResponse message) {
        messagingTemplate.convertAndSend("/sub/chat/mate/" + roomId, message);
    }

    private MateChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }
}