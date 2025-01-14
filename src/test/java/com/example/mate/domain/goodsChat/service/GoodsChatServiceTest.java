package com.example.mate.domain.goodsChat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.file.FileUtils;
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
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Location;
import com.example.mate.domain.goodsPost.entity.Role;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
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
    private GoodsChatMessageRepository messageRepository;

    @Mock
    private GoodsChatEventPublisher eventPublisher;

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
                .location(createLocation())
                .build();
    }

    private Location createLocation() {
        return Location.builder()
                .placeName("test place name")
                .longitude("test longitude")
                .latitude("test latitude")
                .build();
    }

    private GoodsPostImage createGoodsPostImage(GoodsPost goodsPost) {
        return GoodsPostImage.builder()
                .post(goodsPost)
                .imageUrl("main_image_url")
                .build();
    }

    private GoodsChatRoom createGoodsChatRoom(Long id, GoodsPost goodsPost) {
        return GoodsChatRoom.builder()
                .id(id)
                .goodsPost(goodsPost)
                .build();
    }

    private GoodsChatMessage createMessage(GoodsChatRoom chatRoom, String id, int idx, String content, LocalDateTime sentAt) {
        GoodsChatPart goodsChatPart = chatRoom.getChatParts().get(idx);

        return GoodsChatMessage.builder()
                .id(id)
                .memberId(goodsChatPart.getMember().getId())
                .chatRoomId(chatRoom.getId())
                .content(content)
                .sentAt(sentAt)
                .messageType(MessageType.TALK)
                .build();
    }


    @Nested
    @DisplayName("굿즈거래 채팅방 생성 테스트")
    class GoodsChatRoomCreateTest {

        @Test
        @DisplayName("굿즈거래 채팅방 생성 성공 - 기존 채팅방이 있을 경우 해당 채팅방과 채팅내역을 반환한다.")
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

            GoodsChatMessage message = createMessage(existingChatRoom, "1", 0, "test Message", LocalDateTime.now());
            PageImpl<GoodsChatMessage> goodsChatMessages = new PageImpl<>(List.of(message));

            when(memberRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
            when(goodsPostRepository.findById(goodsPostId)).thenReturn(Optional.of(goodsPost));
            when(chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER)).thenReturn(Optional.of(existingChatRoom));
            when(messageRepository.getChatMessages(existingChatRoom.getId(), PageRequest.of(0, 20))).thenReturn(goodsChatMessages);

            // when
            GoodsChatRoomResponse result = goodsChatService.getOrCreateGoodsChatRoom(buyerId, goodsPostId);

            // then
            assertThat(result.getChatRoomId()).isEqualTo(existingChatRoom.getId());
            assertThat(result.getGoodsPostId()).isEqualTo(goodsPost.getId());
            assertThat(result.getPostStatus()).isEqualTo(goodsPost.getStatus().getValue());

            verify(memberRepository, times(2)).findById(buyerId);
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
            verify(eventPublisher).publish(any(GoodsChatEvent.class));
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

            GoodsChatMessage firstMessage = createMessage(chatRoom, "1", 0, "first message", LocalDateTime.now().minusMinutes(10));
            GoodsChatMessage secondMessage = createMessage(chatRoom, "2", 1, "second message", LocalDateTime.now());

            Page<GoodsChatMessage> messagePage = new PageImpl<>(List.of(secondMessage, firstMessage));

            when(partRepository.existsById(goodsChatPartId)).thenReturn(true);
            when(messageRepository.getChatMessages(chatRoomId, pageable)).thenReturn(messagePage);
            when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

            // when
            PageResponse<GoodsChatMessageResponse> result = goodsChatService.getChatRoomMessages(chatRoomId, memberId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getMessage()).isEqualTo(secondMessage.getContent());
            assertThat(result.getContent().get(0).getChatMessageId()).isEqualTo(secondMessage.getId());
            assertThat(result.getContent().get(0).getSenderId()).isEqualTo(memberId);
            assertThat(result.getContent().get(1).getMessage()).isEqualTo(firstMessage.getContent());
            assertThat(result.getContent().get(1).getChatMessageId()).isEqualTo(firstMessage.getId());

            verify(partRepository).existsById(goodsChatPartId);
            verify(messageRepository).getChatMessages(chatRoomId, pageable);
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
            assertThatThrownBy(() -> goodsChatService.getChatRoomMessages(chatRoomId, memberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage());

            // then
            verify(partRepository).existsById(goodsChatPartId);
            verify(messageRepository, never()).getChatMessages(chatRoomId, pageable);
        }
    }

    @Nested
    @DisplayName("채팅방 정보 조회 테스트")
    class GoodsChatRoomInfoTest {

        @Test
        @DisplayName("채팅방 정보 조회 성공")
        void getGoodsChatRoomInfo_should_return_chatroom_info_and_latest_message() {
            // given
            Member member = createMember(1L, "Test Member", "test_member");
            GoodsPost goodsPost = createGoodsPost(1L, member, null, Status.OPEN);
            goodsPost.changeImages(List.of(createGoodsPostImage(goodsPost)));
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, goodsPost);

            Long memberId = member.getId();
            Long chatRoomId = chatRoom.getId();
            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(memberId, chatRoomId);

            chatRoom.addChatParticipant(member, Role.BUYER);
            chatRoom.addChatParticipant(member, Role.SELLER);

            GoodsChatMessage firstMessage = createMessage(chatRoom, "1", 0, "first message", LocalDateTime.now().minusMinutes(10));
            GoodsChatMessage secondMessage = createMessage(chatRoom, "2", 1, "second message", LocalDateTime.now());

            Page<GoodsChatMessage> messages = new PageImpl<>(List.of(secondMessage, firstMessage));

            when(partRepository.existsById(goodsChatPartId)).thenReturn(true);
            when(chatRoomRepository.findByChatRoomId(chatRoomId)).thenReturn(Optional.of(chatRoom));
            when(messageRepository.getChatMessages(chatRoomId, PageRequest.of(0, 20))).thenReturn(messages);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // when
            GoodsChatRoomResponse goodsChatRoomInfo = goodsChatService.getGoodsChatRoomInfo(memberId, chatRoomId);

            // then
            assertThat(goodsChatRoomInfo.getChatRoomId()).isEqualTo(chatRoomId);
            assertThat(goodsChatRoomInfo.getGoodsPostId()).isEqualTo(goodsPost.getId());
            assertThat(goodsChatRoomInfo.getTeamName()).isEqualTo("KIA");
            assertThat(goodsChatRoomInfo.getTitle()).isEqualTo(goodsPost.getTitle());
            assertThat(goodsChatRoomInfo.getCategory()).isEqualTo(goodsPost.getCategory().getValue());
            assertThat(goodsChatRoomInfo.getPrice()).isEqualTo(goodsPost.getPrice());
            assertThat(goodsChatRoomInfo.getPostStatus()).isEqualTo(goodsPost.getStatus().getValue());
            assertThat(goodsChatRoomInfo.getChatRoomStatus()).isEqualTo(chatRoom.getIsActive().toString());
            assertThat(goodsChatRoomInfo.getImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl(goodsPost.getMainImageUrl()));

            assertThat(goodsChatRoomInfo.getInitialMessages().getContent())
                    .hasSize(2)
                    .extracting(GoodsChatMessageResponse::getMessage)
                    .containsExactly("second message", "first message");

            verify(partRepository).existsById(goodsChatPartId);
            verify(chatRoomRepository).findByChatRoomId(chatRoomId);
            verify(messageRepository).getChatMessages(chatRoomId, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("채팅방 정보 조회 실패 - 채팅방에 참여하지 않은 회원")
        void getGoodsChatRoomInfo_should_throw_CustomException_for_not_participant() {
            // given
            Member member = createMember(1L, "Test Member", "test_member");
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, null);

            Long memberId = member.getId();
            Long chatRoomId = chatRoom.getId();

            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(memberId, chatRoomId);
            when(partRepository.existsById(goodsChatPartId)).thenReturn(false);

            // when
            assertThatThrownBy(() -> goodsChatService.getGoodsChatRoomInfo(memberId, chatRoomId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage());

            // then
            verify(partRepository).existsById(goodsChatPartId);
            verify(chatRoomRepository, never()).findByChatRoomId(chatRoomId);
            verify(messageRepository, never()).getChatMessages(chatRoomId, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("채팅방 정보 조회 실패 - 존재하지 않는 채팅방")
        void getGoodsChatRoomInfo_should_throw_CustomException_for_invalid_chatroom() {
            Member member = createMember(1L, "Test Member", "test_member");
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, null);

            Long memberId = member.getId();
            Long chatRoomId = chatRoom.getId();

            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(memberId, chatRoomId);
            when(partRepository.existsById(goodsChatPartId)).thenReturn(true);
            when(chatRoomRepository.findByChatRoomId(chatRoomId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsChatService.getGoodsChatRoomInfo(memberId, chatRoomId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND.getMessage());

            // then
            verify(partRepository).existsById(goodsChatPartId);
            verify(chatRoomRepository).findByChatRoomId(chatRoomId);
            verify(messageRepository, never()).getChatMessages(chatRoomId, PageRequest.of(0, 20));
        }
    }

    @Nested
    @DisplayName("채팅방 목록 조회 테스트")
    class GoodsChatRoomListTest {

        @Test
        @DisplayName("채팅방 목록 조회 성공")
        void getGoodsChatRooms_should_return_paginated_chatRoom_summaries() {
            // given
            Long memberId = 1L;
            Member member = createMember(memberId, "Test Member", "test_member");
            Member opponentMember = createMember(2L, "Opponent Member", "opponent_member");

            // 첫번째 채팅방
            GoodsPost goodsPost = createGoodsPost(1L, member, null, Status.OPEN);
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, goodsPost);
            chatRoom.addChatParticipant(member, Role.BUYER);
            chatRoom.addChatParticipant(opponentMember, Role.SELLER);

            // 두번째 채팅방
            GoodsPost goodsPost2 = createGoodsPost(2L, member, null, Status.OPEN);
            GoodsChatRoom chatRoom2 = createGoodsChatRoom(2L, goodsPost2);
            chatRoom2.addChatParticipant(member, Role.BUYER);
            chatRoom2.addChatParticipant(opponentMember, Role.SELLER);

            Pageable pageable = PageRequest.of(0, 10);
            Page<GoodsChatRoom> chatRoomPage = new PageImpl<>(List.of(chatRoom, chatRoom2), pageable, 2);

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(chatRoomRepository.findChatRoomPageByMemberId(memberId, pageable)).thenReturn(chatRoomPage);

            // when
            PageResponse<GoodsChatRoomSummaryResponse> result = goodsChatService.getGoodsChatRooms(memberId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getPageNumber()).isEqualTo(0);

            List<GoodsChatRoomSummaryResponse> resultContent = result.getContent();
            assertThat(resultContent.get(0).getChatRoomId()).isEqualTo(chatRoom.getId());
            assertThat(resultContent.get(0).getOpponentNickname()).isEqualTo(opponentMember.getNickname());
            assertThat(resultContent.get(1).getChatRoomId()).isEqualTo(chatRoom2.getId());
            assertThat(resultContent.get(1).getOpponentNickname()).isEqualTo(opponentMember.getNickname());

            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository).findChatRoomPageByMemberId(memberId, pageable);
        }

        @Test
        @DisplayName("채팅방 목록 조회 실패 - 존재하지 않는 회원")
        void getGoodsChatRooms_should_throw_CustomException_for_invalid_member() {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> goodsChatService.getGoodsChatRooms(memberId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

            // then
            verify(memberRepository).findById(memberId);
            verify(chatRoomRepository, never()).findChatRoomPageByMemberId(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("채팅방 채팅내역 조회 테스트")
    class GoodsChatMessageFetchTest {

        @Test
        @DisplayName("채팅방 채팅내역 조회 성공 - 최근 채팅내역을 페이지로 반환")
        void getMessagesForChatRoom_should_return_paginated_messages() {
            // given
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, null);
            Member buyer = createMember(1L, "Test buyer", "test_buyer");
            Member seller = createMember(2L, "Test seller", "test_seller");
            chatRoom.addChatParticipant(buyer, Role.BUYER);
            chatRoom.addChatParticipant(seller, Role.SELLER);

            Long chatRoomId = chatRoom.getId();
            Long buyerId = buyer.getId();

            Pageable pageable = PageRequest.of(0, 20);
            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(buyerId, chatRoomId);

            GoodsChatMessage firstMessage = createMessage(chatRoom, "1", 0, "First message", LocalDateTime.now().minusMinutes(10));
            GoodsChatMessage secondMessage = createMessage(chatRoom, "2", 1, "Second message", LocalDateTime.now());

            Page<GoodsChatMessage> messagePage = new PageImpl<>(List.of(secondMessage, firstMessage), pageable, 2);

            when(partRepository.existsById(goodsChatPartId)).thenReturn(true);
            when(messageRepository.getChatMessages(chatRoomId, pageable)).thenReturn(messagePage);
            when(memberRepository.findById(1L)).thenReturn(Optional.of(buyer));
            when(memberRepository.findById(2L)).thenReturn(Optional.of(seller));

            // when
            PageResponse<GoodsChatMessageResponse> result = goodsChatService.getChatRoomMessages(chatRoomId, buyerId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getMessage()).isEqualTo(secondMessage.getContent());
            assertThat(result.getContent().get(0).getChatMessageId()).isEqualTo(secondMessage.getId());
            assertThat(result.getContent().get(0).getSentAt()).isEqualTo(secondMessage.getSentAt());
            assertThat(result.getContent().get(1).getMessage()).isEqualTo(firstMessage.getContent());
            assertThat(result.getContent().get(1).getChatMessageId()).isEqualTo(firstMessage.getId());
            assertThat(result.getContent().get(1).getSentAt()).isEqualTo(firstMessage.getSentAt());

            verify(partRepository).existsById(goodsChatPartId);
            verify(messageRepository).getChatMessages(chatRoomId, pageable);
        }

        @Test
        @DisplayName("채팅 메시지 조회 실패 - 회원이 채팅방에 참여하지 않은 경우 예외를 발생시킨다.")
        void getMessagesForChatRoom_should_throw_exception_when_member_not_participant() {
            // given
            GoodsChatRoom chatRoom = createGoodsChatRoom(1L, null);
            Member buyer = createMember(1L, "Test buyer", "test_buyer");
            Member seller = createMember(2L, "Test seller", "test_seller");
            chatRoom.addChatParticipant(buyer, Role.BUYER);
            chatRoom.addChatParticipant(seller, Role.SELLER);

            Long chatRoomId = chatRoom.getId();
            Long buyerId = buyer.getId();

            Pageable pageable = PageRequest.of(0, 20);
            GoodsChatPartId goodsChatPartId = new GoodsChatPartId(buyerId, chatRoomId);

            when(partRepository.existsById(goodsChatPartId)).thenReturn(false);

            // when
            assertThatThrownBy(() -> goodsChatService.getChatRoomMessages(chatRoomId, buyerId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage());

            // then
            verify(partRepository).existsById(goodsChatPartId);
            verify(messageRepository, never()).getChatMessages(anyLong(), any(Pageable.class));
        }

        @Nested
        @DisplayName("굿즈 채팅방 퇴장 테스트")
        class GoodsChatroomLeaveTest {

            @Test
            @DisplayName("채팅방 퇴장 성공 - 남아있는 참여자가 있는 경우 퇴장 메시지 전송")
            void deactivateGoodsChatPart_should_publish_leave_event_when_other_members_remain() {
                // given
                Long memberId = 1L;
                Long chatRoomId = 1L;
                GoodsChatRoom chatRoom = createGoodsChatRoom(chatRoomId, null);

                Member member = createMember(memberId, "Test Member", "test_member");
                Member anotherMember = createMember(2L, "Another Member", "another_member");

                chatRoom.addChatParticipant(member, Role.BUYER);
                chatRoom.addChatParticipant(anotherMember, Role.SELLER);

                GoodsChatPart goodsChatPart = chatRoom.getChatParts().get(0);

                when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
                when(partRepository.findById(new GoodsChatPartId(memberId, chatRoomId))).thenReturn(Optional.of(goodsChatPart));

                // when
                goodsChatService.deactivateGoodsChatPart(memberId, chatRoomId);

                // then
                assertThat(goodsChatPart.getIsActive()).isFalse();
                assertThat(chatRoom.getIsActive()).isFalse();
                verify(memberRepository).findById(memberId);
                verify(partRepository).findById(new GoodsChatPartId(memberId, chatRoomId));
                verify(chatRoomRepository, never()).deleteById(chatRoomId);
                verify(eventPublisher).publish(any(GoodsChatEvent.class));
            }

            @Test
            @DisplayName("채팅방 퇴장 성공 - 채팅방에 남아있는 참여자가 없는 경우 채팅방 삭제")
            void deactivateGoodsChatPart_should_delete_chat_room_when_no_members_remain() {
                // given
                Long memberId = 1L;
                Long chatRoomId = 1L;
                GoodsChatRoom chatRoom = createGoodsChatRoom(chatRoomId, null);

                Member member = createMember(memberId, "Test Member", "test_member");
                Member anotherMember = createMember(2L, "Another Member", "another_member");

                chatRoom.addChatParticipant(member, Role.BUYER);
                chatRoom.addChatParticipant(anotherMember, Role.SELLER);

                GoodsChatPart goodsChatPart = chatRoom.getChatParts().get(0);

                // 미리  anotherMember 는 채팅방을 나가도록 설정 (채팅방 비활성화)
                chatRoom.getChatParts().get(1).leaveAndCheckRoomStatus();

                when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
                when(partRepository.findById(new GoodsChatPartId(memberId, chatRoomId))).thenReturn(Optional.of(goodsChatPart));

                // when
                goodsChatService.deactivateGoodsChatPart(memberId, chatRoomId);

                // then
                verify(memberRepository).findById(memberId);
                verify(partRepository).findById(new GoodsChatPartId(memberId, chatRoomId));
                verify(chatRoomRepository).deleteById(chatRoomId);
                verify(eventPublisher, never()).publish(any(GoodsChatEvent.class));
            }

            @Test
            @DisplayName("채팅방 퇴장 실패 - 존재하지 않는 회원일 경우 예외 발생")
            void deactivateGoodsChatPart_should_throw_exception_when_member_not_found() {
                // given
                Long memberId = 1L;
                Long chatRoomId = 1L;

                when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> goodsChatService.deactivateGoodsChatPart(memberId, chatRoomId))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage());

                verify(memberRepository).findById(memberId);
                verify(partRepository, never()).findById(any(GoodsChatPartId.class));
                verify(chatRoomRepository, never()).deleteById(anyLong());
                verify(eventPublisher, never()).publish(any(GoodsChatEvent.class));
            }

            @Test
            @DisplayName("채팅방 퇴장 실패 - 참여하지 않은 회원이 퇴장을 시도할 경우 예외 발생")
            void deactivateGoodsChatPart_should_throw_exception_when_member_not_in_chatroom() {
                // given
                Long memberId = 1L;
                Long chatRoomId = 1L;
                Member member = createMember(memberId, "Test Member", "test_member");

                when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
                when(partRepository.findById(new GoodsChatPartId(memberId, chatRoomId))).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> goodsChatService.deactivateGoodsChatPart(memberId, chatRoomId))
                        .isInstanceOf(CustomException.class)
                        .hasMessage(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage());

                verify(memberRepository).findById(memberId);
                verify(partRepository).findById(new GoodsChatPartId(memberId, chatRoomId));
                verify(chatRoomRepository, never()).deleteById(chatRoomId);
                verify(eventPublisher, never()).publish(any(GoodsChatEvent.class));
            }
        }
    }

    @Nested
    @DisplayName("채팅방 참여자 조회 테스트")
    class GoodsChatRoomMemberTest {

        @Test
        @DisplayName("채팅방 참여자 조회 성공 - 참여자가 존재하는 경우 참여자 목록을 반환")
        void getChatRoomMembers_should_return_list_of_participants() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;
            GoodsChatRoom goodsChatRoom = createGoodsChatRoom(chatRoomId, null);

            Member member = createMember(memberId, "Test Member", "test_member");
            Member anotherMember = createMember(2L, "Test Another Member", "test_another_member");

            goodsChatRoom.addChatParticipant(member, Role.BUYER);
            goodsChatRoom.addChatParticipant(anotherMember, Role.SELLER);

            when(partRepository.existsById(new GoodsChatPartId(memberId, chatRoomId))).thenReturn(true);
            when(partRepository.findAllWithMemberByChatRoomId(chatRoomId)).thenReturn(goodsChatRoom.getChatParts());

            // when
            List<MemberSummaryResponse> result = goodsChatService.getMembersInChatRoom(memberId, chatRoomId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getMemberId()).isEqualTo(member.getId());
            assertThat(result.get(0).getNickname()).isEqualTo(member.getNickname());
            assertThat(result.get(1).getMemberId()).isEqualTo(anotherMember.getId());
            assertThat(result.get(1).getNickname()).isEqualTo(anotherMember.getNickname());

            verify(partRepository).existsById(new GoodsChatPartId(memberId, chatRoomId));
            verify(partRepository).findAllWithMemberByChatRoomId(chatRoomId);
        }

        @Test
        @DisplayName("채팅방 참여자 조회 실패 - 참여하지 않은 회원이 참여자 목록을 요청한 경우 예외 발생")
        void getChatRoomMembers_should_throw_exception_for_non_participant() {
            // given
            Long memberId = 1L;
            Long chatRoomId = 1L;

            when(partRepository.existsById(new GoodsChatPartId(memberId, chatRoomId))).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> goodsChatService.getMembersInChatRoom(memberId, chatRoomId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART.getMessage());

            verify(partRepository).existsById(new GoodsChatPartId(memberId, chatRoomId));
            verify(partRepository, never()).findAllWithMemberByChatRoomId(chatRoomId);
        }
    }
}