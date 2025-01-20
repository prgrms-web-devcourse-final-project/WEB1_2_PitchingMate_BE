package com.example.mate.domain.goodsChat.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import com.example.mate.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.event.GoodsChatEvent;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class GoodsChatMessageServiceTest {

    @InjectMocks
    private GoodsChatMessageService goodsChatMessageService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsChatRoomRepository chatRoomRepository;

    @Mock
    private GoodsChatMessageRepository messageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private Member createMember(Long id, String name, String nickname) {
        return Member.builder()
                .id(id)
                .name(name)
                .nickname(nickname)
                .build();
    }

    private GoodsChatRoom createGoodsChatRoom(Long id) {
        return GoodsChatRoom.builder()
                .id(id)
                .build();
    }

    private GoodsChatPart createGoodsChatPart(Member member, GoodsChatRoom chatRoom) {
        return GoodsChatPart.builder()
                .member(member)
                .goodsChatRoom(chatRoom)
                .build();
    }

    private GoodsChatMessage createGoodsChatMessage(String message, GoodsChatPart chatPart, MessageType type) {
        return GoodsChatMessage.builder()
                .content(message)
                .chatRoomId(chatPart.getGoodsChatRoom().getId())
                .memberId(chatPart.getMember().getId())
                .messageType(type)
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("굿즈거래 채팅 전송 테스트")
    class SendChatMessageTest {

        @Test
        @DisplayName("메시지 전송 성공")
        void sendMessage_should_save_message_and_send_to_subscribers() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            GoodsChatMessageRequest request = new GoodsChatMessageRequest(chatRoomId, memberId, "Hello World", MessageType.TALK);

            Member member = createMember(memberId, "Test User", "test_user");
            GoodsChatRoom chatRoom = createGoodsChatRoom(chatRoomId);
            GoodsChatPart chatPart = createGoodsChatPart(member, chatRoom);
            GoodsChatMessage chatMessage = createGoodsChatMessage(request.getMessage(), chatPart, MessageType.TALK);

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(messageRepository.save(any(GoodsChatMessage.class))).thenReturn(chatMessage);

            // when
            goodsChatMessageService.sendMessage(request);

            // then
            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository).findById(chatRoomId);
            verify(messageRepository).save(any(GoodsChatMessage.class));
            verify(messagingTemplate).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }

        @Test
        @DisplayName("메시지 전송 실패 - 유효하지 않은 회원")
        void sendMessage_should_throw_custom_exception_for_invalid_member() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            GoodsChatMessageRequest request = new GoodsChatMessageRequest(chatRoomId, memberId, "Hello World", MessageType.TALK);

            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsChatMessageService.sendMessage(request))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            // then
            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository, never()).findById(chatRoomId);
            verify(messageRepository, never()).save(any(GoodsChatMessage.class));
            verify(messagingTemplate, never()).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }

        @Test
        @DisplayName("메시지 전송 실패 - 유효하지 않은 채팅방")
        void sendMessage_should_throw_custom_exception_for_invalid_chatroom() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            GoodsChatMessageRequest request = new GoodsChatMessageRequest(chatRoomId, memberId, "Hello World", MessageType.TALK);

            Member member = createMember(memberId, "Test User", "test_user");

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsChatMessageService.sendMessage(request))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND.getMessage());

            // then
            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository).findById(chatRoomId);
            verify(messageRepository, never()).save(any(GoodsChatMessage.class));
            verify(messagingTemplate, never()).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }

        @Test
        @DisplayName("메시지 전송 실패 - 해당 채팅방에 참가한 회원이 아닌 경우")
        void sendMessage_should_throw_custom_exception_for_non_participant() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            GoodsChatMessageRequest request = new GoodsChatMessageRequest(chatRoomId, memberId, "Hello World", MessageType.TALK);
            Member member = createMember(memberId, "Test User", "test_user");

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsChatMessageService.sendMessage(request))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND.getMessage());

            // then
            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository).findById(chatRoomId);
            verify(messageRepository, never()).save(any(GoodsChatMessage.class));
            verify(messagingTemplate, never()).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }
    }

    @Nested
    @DisplayName("굿즈거래 입장 및 퇴장 메시지 전송 테스트")
    class SendChatSystemMessageTest {

        @Test
        @DisplayName("채팅방 입장 메시지 전송 성공")
        void sendChatEventMessage_should_send_enter_message() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;

            Member member = createMember(memberId, "Test User", "test_user");
            GoodsChatEvent event = new GoodsChatEvent(chatRoomId, member, MessageType.ENTER);

            GoodsChatRoom chatRoom = createGoodsChatRoom(event.chatRoomId());
            GoodsChatPart chatPart = createGoodsChatPart(event.member(), chatRoom);

            GoodsChatMessage chatMessage
                    = createGoodsChatMessage(member.getNickname() + "님이 대화를 시작했습니다.", chatPart, event.type());

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(messageRepository.save(any(GoodsChatMessage.class))).thenReturn(chatMessage);

            // when
            goodsChatMessageService.sendChatEventMessage(event);

            // then
            verify(chatRoomRepository).findById(chatRoomId);
            verify(messageRepository).save(any(GoodsChatMessage.class));
            verify(messagingTemplate).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }

        @Test
        @DisplayName("채팅방 퇴장 메시지 전송 성공")
        void sendChatEventMessage_should_send_leave_message() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;

            Member member = createMember(memberId, "Test User", "test_user");
            GoodsChatEvent event = new GoodsChatEvent(chatRoomId, member, MessageType.LEAVE);

            GoodsChatRoom chatRoom = createGoodsChatRoom(event.chatRoomId());
            GoodsChatPart chatPart = createGoodsChatPart(event.member(), chatRoom);

            GoodsChatMessage chatMessage
                    = createGoodsChatMessage(member.getNickname() + "님이 대화를 떠났습니다.", chatPart, event.type());

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(messageRepository.save(any(GoodsChatMessage.class))).thenReturn(chatMessage);

            // when
            goodsChatMessageService.sendChatEventMessage(event);

            // then
            verify(chatRoomRepository).findById(chatRoomId);
            verify(messageRepository).save(any(GoodsChatMessage.class));
            verify(messagingTemplate).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }

        @Test
        @DisplayName("시스템 메시지 전송 실패 - 유효하지 않은 채팅방")
        void sendChatEventMessage_should_throw_custom_exception_for_invalid_chatroom() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;

            Member member = createMember(memberId, "Test User", "test_user");
            GoodsChatEvent event = new GoodsChatEvent(chatRoomId, member, MessageType.LEAVE);

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsChatMessageService.sendChatEventMessage(event))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND.getMessage());

            // then
            verify(chatRoomRepository).findById(chatRoomId);
            verify(messageRepository, never()).save(any(GoodsChatMessage.class));
            verify(messagingTemplate, never()).convertAndSend(eq("/sub/chat/goods/" + chatRoomId), any(GoodsChatMessageResponse.class));
        }
    }
}