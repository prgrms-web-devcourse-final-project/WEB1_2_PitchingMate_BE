package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MateChatMessageServiceTest {

    @InjectMocks
    private MateChatMessageService mateChatMessageService;

    @Mock
    private MateChatRoomRepository chatRoomRepository;

    @Mock
    private MateChatMessageRepository chatMessageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    private Member createMember(Long id) {
        return Member.builder()
                .id(id)
                .name("Test User")
                .nickname("test_user")
                .build();
    }

    private MateChatRoom createMateChatRoom(Long id) {
        return MateChatRoom.builder()
                .id(id)
                .isMessageable(true)
                .build();
    }

    private MateChatMessage createMateChatMessage(MateChatRoom chatRoom, Member sender, String content, MessageType type) {
        return MateChatMessage.builder()
                .roomId(chatRoom.getId())
                .senderId(sender.getId())
                .content(content)
                .type(type)
                .sendTime(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("메이트 채팅 메시지 전송 테스트")
    class SendMessageTest {

        @Test
        @DisplayName("메시지 전송 성공")
        void sendMessage_should_save_message_and_send_to_subscribers() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            String message = "Hello World";
            MateChatMessageRequest request = new MateChatMessageRequest(MessageType.TALK.name(), chatRoomId, memberId, message);

            Member sender = createMember(memberId);
            MateChatRoom chatRoom = createMateChatRoom(chatRoomId);
            MateChatMessage chatMessage = createMateChatMessage(chatRoom, sender, message, MessageType.TALK);

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(sender));
            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(chatMessageRepository.save(any(MateChatMessage.class))).thenReturn(chatMessage);

            // when
            mateChatMessageService.sendMessage(request);

            // then
            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository).findById(chatRoomId);
            verify(chatMessageRepository).save(any(MateChatMessage.class));
            verify(messagingTemplate).convertAndSend(eq("/sub/chat/mate/" + chatRoomId), any(MateChatMessageResponse.class));
        }

        @Test
        @DisplayName("메시지 전송 실패 - 유효하지 않은 회원")
        void sendMessage_should_throw_custom_exception_for_invalid_member() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            MateChatMessageRequest request = new MateChatMessageRequest(MessageType.TALK.name(), chatRoomId, memberId, "Hello");

            MateChatRoom chatRoom = createMateChatRoom(chatRoomId);
            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateChatMessageService.sendMessage(request))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            verify(chatRoomRepository).findById(chatRoomId);
            verify(memberRepository).findById(memberId);
            verify(chatMessageRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
        }

        @Test
        @DisplayName("메시지 전송 실패 - 유효하지 않은 채팅방")
        void sendMessage_should_throw_custom_exception_for_invalid_chatroom() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            MateChatMessageRequest request = new MateChatMessageRequest(MessageType.TALK.name(), chatRoomId, memberId, "Hello");

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateChatMessageService.sendMessage(request))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());

            verify(chatRoomRepository).findById(chatRoomId);
            verify(memberRepository, never()).findById(anyLong());
            verify(chatMessageRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
        }
    }

    @Nested
    @DisplayName("메이트 채팅 이벤트 메시지 전송 테스트")
    class SendChatEventMessageTest {

        @Test
        @DisplayName("입장 메시지 전송 성공")
        void sendChatEventMessage_should_send_enter_message() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            Member member = createMember(memberId);
            MateChatRoom chatRoom = createMateChatRoom(chatRoomId);
            MateChatEvent event = MateChatEvent.from(chatRoomId, member, MessageType.ENTER);

            MateChatMessage chatMessage = createMateChatMessage(
                    chatRoom,
                    member,
                    member.getNickname() + "님이 들어왔습니다.",
                    MessageType.ENTER
            );

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(chatMessageRepository.save(any(MateChatMessage.class))).thenReturn(chatMessage);

            // when
            mateChatMessageService.sendChatEventMessage(event);

            // then
            verify(chatRoomRepository).findById(chatRoomId);
            verify(chatMessageRepository).save(any(MateChatMessage.class));
            verify(messagingTemplate).convertAndSend(eq("/sub/chat/mate/" + chatRoomId), any(MateChatMessageResponse.class));
        }

        @Test
        @DisplayName("퇴장 메시지 전송 성공")
        void sendChatEventMessage_should_send_leave_message() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            Member member = createMember(memberId);
            MateChatRoom chatRoom = createMateChatRoom(chatRoomId);
            MateChatEvent event = MateChatEvent.from(chatRoomId, member, MessageType.LEAVE);

            MateChatMessage chatMessage = createMateChatMessage(
                    chatRoom,
                    member,
                    member.getNickname() + "님이 나갔습니다.",
                    MessageType.LEAVE
            );

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(chatMessageRepository.save(any(MateChatMessage.class))).thenReturn(chatMessage);

            // when
            mateChatMessageService.sendChatEventMessage(event);

            // then
            verify(chatRoomRepository).findById(chatRoomId);
            verify(chatMessageRepository).save(any(MateChatMessage.class));
            verify(messagingTemplate).convertAndSend(eq("/sub/chat/mate/" + chatRoomId), any(MateChatMessageResponse.class));
        }

        @Test
        @DisplayName("이벤트 메시지 전송 실패 - 유효하지 않은 채팅방")
        void sendChatEventMessage_should_throw_custom_exception_for_invalid_chatroom() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            Member member = createMember(memberId);
            MateChatEvent event = MateChatEvent.from(chatRoomId, member, MessageType.ENTER);

            when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateChatMessageService.sendChatEventMessage(event))
                    .isExactlyInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.CHAT_ROOM_NOT_FOUND.getMessage());

            verify(chatRoomRepository).findById(chatRoomId);
            verify(chatMessageRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
        }
    }
}