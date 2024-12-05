package com.example.mate.domain.goodsChat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatPartRepository;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GoodsChatServiceTest {

    @InjectMocks
    private GoodsChatService goodsChatService;

    @Mock
    private GoodsPostRepository goodsPostRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoodsChatRoomRepository chatRoomRepository;

    @Mock
    private  GoodsChatPartRepository partRepository;

    @Mock
    private  GoodsChatMessageRepository messageRepository;

    private Member createMember(Long id, String name, String nickname) {
        return Member.builder()
                .id(id)
                .name(name)
                .nickname(nickname)
                .build();
    }

    private GoodsPost createGoodsPost(Long id, Member seller, Member buyer, Status status) {
        return GoodsPost.builder()
                .id(id)
                .seller(seller)
                .buyer(buyer)
                .teamId(1L)
                .title("test title")
                .content("test content")
                .price(10_000)
                .status(status)
                .category(Category.ACCESSORY)
                .build();
    }

    private GoodsChatRoom createGoodsChatRoom(Long id, GoodsPost goodsPost) {
        return GoodsChatRoom.builder()
                .id(id)
                .goodsPost(goodsPost)
                .build();
    }

    private GoodsChatMessage createMessage(GoodsChatRoom chatRoom, Long id, int idx, String content, LocalDateTime sentAt) {
        return GoodsChatMessage.builder()
                .id(id)
                .goodsChatPart(chatRoom.getChatParts().get(idx))
                .content(content)
                .sentAt(sentAt)
                .messageType(MessageType.TALK)
                .build();
    }


    @Nested
    @DisplayName("굿즈거래 채팅방 생성 테스트")
    class GoodsChatRoomCreateTest {

        @Test
        @DisplayName("굿즈거래 채팅방 생성 성공 - 기존 채팅방이 있을 경우 해당 채팅방을 반환한다.")
        void get_Or_Create_GoodsChatRoom_should_return_existing_chatroom() {
            // given
            Member buyer = createMember(1L, "test buyer", "test buyer nickname");
            Member seller = createMember(2L, "test seller", "test seller nickname");

            Long buyerId = buyer.getId();
            GoodsPost goodsPost = createGoodsPost(1L, seller, null, Status.OPEN);
            Long goodsPostId = 1L;

            GoodsChatRoom existingChatRoom = createGoodsChatRoom(1L, goodsPost);
            existingChatRoom.addChatParticipant(buyer, Role.BUYER);
            existingChatRoom.addChatParticipant(seller, Role.SELLER);

            when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
            when(goodsPostRepository.findById(goodsPostId)).thenReturn(Optional.of(goodsPost));
            when(chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER))
                    .thenReturn(Optional.of(existingChatRoom));

            // when
            GoodsChatRoomResponse result = goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId);

            // then
            assertThat(result.getChatRoomId()).isEqualTo(existingChatRoom.getId());
            assertThat(result.getGoodsPostId()).isEqualTo(goodsPost.getId());
            assertThat(result.getPostStatus()).isEqualTo(goodsPost.getStatus().getValue());

            verify(memberRepository).findById(buyerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(chatRoomRepository).findExistingChatRoom(goodsPostId, buyerId, Role.BUYER);
            verify(chatRoomRepository, never()).save(any());
        }

        @Test
        @DisplayName("굿즈거래 채팅방 생성 성공 - 기존 채팅방이 없을 경우 새로운 채팅방을 생성한다.")
        void get_Or_Create_GoodsChatRoom_should_create_new_chatRoom() {
            // given
            Member buyer = createMember(1L, "test buyer", "test buyer nickname");
            Member seller = createMember(2L, "test seller", "test seller nickname");

            Long buyerId = buyer.getId();
            GoodsPost goodsPost = createGoodsPost(1L, seller, null, Status.OPEN);
            Long goodsPostId = goodsPost.getId();

            GoodsChatRoom newGoodsChatRoom = createGoodsChatRoom(1L, goodsPost);
            newGoodsChatRoom.addChatParticipant(buyer, Role.BUYER);
            newGoodsChatRoom.addChatParticipant(seller, Role.SELLER);

            when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
            when(goodsPostRepository.findById(goodsPostId)).thenReturn(Optional.of(goodsPost));
            when(chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER)).thenReturn(Optional.empty());
            when(chatRoomRepository.save(any(GoodsChatRoom.class))).thenReturn(newGoodsChatRoom);

            // when
            GoodsChatRoomResponse result = goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId);

            // then
            assertThat(result.getChatRoomId()).isEqualTo(newGoodsChatRoom.getId());
            assertThat(result.getGoodsPostId()).isEqualTo(goodsPost.getId());
            assertThat(result.getPostStatus()).isEqualTo(goodsPost.getStatus().getValue());

            verify(memberRepository).findById(buyerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(chatRoomRepository).findExistingChatRoom(goodsPostId, buyerId, Role.BUYER);
            verify(chatRoomRepository).save(any(GoodsChatRoom.class));
        }

        @Test
        @DisplayName("굿즈거래 채팅방 생성 실패 - 거래 완료된 판매글일 경우 예외를 발생시킨다.")
        void get_Or_Create_GoodsChatRoom_failed_with_closed_post() {
            // given
            Member buyer = createMember(1L, "test buyer", "test buyer nickname");
            Member seller = createMember(2L, "test seller", "test seller nickname");

            Long buyerId = buyer.getId();
            GoodsPost goodsPost = createGoodsPost(1L, seller, buyer, Status.CLOSED);
            Long goodsPostId = goodsPost.getId();

            when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
            when(goodsPostRepository.findById(goodsPostId)).thenReturn(Optional.of(goodsPost));

            // when
            assertThatThrownBy(() -> goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_CLOSED_POST.getMessage());

            // then
            verify(memberRepository).findById(buyerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(chatRoomRepository, never()).findExistingChatRoom(anyLong(), anyLong(), any(Role.class));
            verify(chatRoomRepository, never()).save(any(GoodsChatRoom.class));
        }

        @Test
        @DisplayName("굿즈거래 채팅방 생성 실패 - 구매자가 판매글의 판매자라면 예외를 발생시킨다.")
        void get_Or_Create_GoodsChatRoom_failed_with_seller_as_buyer() {
            // given
            Member buyer = createMember(1L, "test buyer", "test buyer nickname");
            Long buyerId = buyer.getId();

            GoodsPost goodsPost = createGoodsPost(1L, buyer, null, Status.OPEN);
            Long goodsPostId = goodsPost.getId();

            when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
            when(goodsPostRepository.findById(goodsPostId)).thenReturn(Optional.of(goodsPost));

            // when
            assertThatThrownBy(() -> goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_SELLER_CANNOT_START.getMessage());

            // then
            verify(memberRepository).findById(buyerId);
            verify(goodsPostRepository).findById(goodsPostId);
            verify(chatRoomRepository, never()).findExistingChatRoom(anyLong(), anyLong(), any(Role.class));
            verify(chatRoomRepository, never()).save(any(GoodsChatRoom.class));
        }
    }

    @Nested
    @DisplayName("채팅 내역 조회 테스트")
    class GoodsChatMessagePageTest {

        @Test
        @DisplayName("채팅 내역 조회 성공 - 회원이 채팅방에 참여한 경우 메시지를 페이지로 반환한다.")
        void getMessagesForChatRoom_should_return_messages() {
            // given
            Member member = createMember(2L, "Test Member", "test_member");
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, null);
            Long memberId = member.getId();
            Long chatRoomId = chatRoom.getId();
            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(memberId, chatRoomId);

            chatRoom.addChatParticipant(member, Role.BUYER);
            chatRoom.addChatParticipant(member, Role.SELLER);

            Pageable pageable = PageRequest.of(0, 10);

            GoodsChatMessage firstMessage = createMessage(chatRoom, 1L, 0, "first message", LocalDateTime.now().minusMinutes(10));
            GoodsChatMessage secondMessage = createMessage(chatRoom, 2L, 1, "second message", LocalDateTime.now());

            Page<GoodsChatMessage> messagePage = new PageImpl<>(List.of(secondMessage, firstMessage));

            when(partRepository.existsById(goodsChatPartId)).thenReturn(true);
            when(messageRepository.findByChatRoomId(chatRoomId, pageable)).thenReturn(messagePage);

            // when
            PageResponse<GoodsChatMessageResponse> result = goodsChatService.getMessagesForChatRoom(chatRoomId, memberId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getMessage()).isEqualTo(secondMessage.getContent());
            assertThat(result.getContent().get(0).getChatMessageId()).isEqualTo(secondMessage.getId());
            assertThat(result.getContent().get(0).getSenderId()).isEqualTo(memberId);
            assertThat(result.getContent().get(1).getMessage()).isEqualTo(firstMessage.getContent());
            assertThat(result.getContent().get(1).getChatMessageId()).isEqualTo(firstMessage.getId());

            verify(partRepository).existsById(goodsChatPartId);
            verify(messageRepository).findByChatRoomId(chatRoomId, pageable);
        }

        @Test
        @DisplayName("채팅 내역 조회 실패 - 회원이 채팅방에 참여하지 않은 경우 예외를 발생시킨다.")
        void getMessagesForChatRoom_should_throw_exception_for_non_participant() {
            // given
            Long chatRoomId = 1L;
            Long memberId = 2L;
            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(memberId, chatRoomId);
            Pageable pageable = PageRequest.of(0, 10);

            when(partRepository.existsById(goodsChatPartId)).thenReturn(false);

            // when
            assertThatThrownBy(() -> goodsChatService.getMessagesForChatRoom(chatRoomId, memberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage());

            // then
            verify(partRepository).existsById(goodsChatPartId);
            verify(messageRepository, never()).findByChatRoomId(chatRoomId, pageable);
        }
    }
}