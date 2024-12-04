package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
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


    public void sendMessage(GoodsChatMessageRequest message) {
        Member sender = findMemberById(message.getSenderId());
        GoodsChatRoom chatRoom = findByChatRoomById(message.getRoomId());
        GoodsChatPart chatPart = findByChatPartById(sender, chatRoom);

        // DB에 메시지 저장
        GoodsChatMessage chatMessage
                = messageRepository.save(GoodsChatMessageRequest.from(message, chatPart, MessageType.TALK));
        chatRoom.updateLastChat(chatMessage.getContent(), chatMessage.getSentAt());

        GoodsChatMessageResponse response = GoodsChatMessageResponse.of(chatMessage);
        sendToSubscribers(message.getRoomId(), response);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private GoodsChatRoom findByChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND));
    }

    private GoodsChatPart findByChatPartById(Member sender, GoodsChatRoom chatRoom) {
        return chatPartRepository.findById(new GoodsChatPartId(sender.getId(), chatRoom.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART));
    }

    private void sendToSubscribers(Long roomId, GoodsChatMessageResponse message) {
        messagingTemplate.convertAndSend("/sub/chat/goods/" + roomId, message);
    }
}
