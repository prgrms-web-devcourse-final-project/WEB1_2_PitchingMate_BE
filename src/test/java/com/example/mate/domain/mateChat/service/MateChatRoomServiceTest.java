package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import com.example.mate.domain.mateChat.event.MateChatEvent;
import com.example.mate.domain.mateChat.event.MateChatEventPublisher;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomMemberRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MateChatRoomServiceTest {

    @InjectMocks
    private MateChatRoomService chatRoomService;

    @Mock
    private MateChatRoomRepository chatRoomRepository;

    @Mock
    private MateChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private MateChatMessageRepository chatMessageRepository;

    @Mock
    private MateRepository mateRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MateChatEventPublisher eventPublisher;

    @Test
    @DisplayName("메이트 게시글에서 채팅방 생성 성공 - 기존 채팅방이 없는 경우")
    void createChatRoomFromPost_Success() {
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Member member = createMember(memberId, "member1", 25, Gender.MALE);
        Member author = createMember(2L, "author", 25, Gender.MALE);
        MatePost matePost = createMatePost(postId, author);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(mateRepository.findById(postId)).willReturn(Optional.of(matePost));
        given(chatRoomRepository.findByMatePostId(postId)).willReturn(Optional.empty());
        given(chatRoomRepository.save(any(MateChatRoom.class))).willAnswer(invocation -> {
            MateChatRoom chatRoom = invocation.getArgument(0);
            ReflectionTestUtils.setField(chatRoom, "id", 1L);
            return chatRoom;
        });
        given(chatRoomMemberRepository.save(any(MateChatRoomMember.class))).willAnswer(invocation -> {
            MateChatRoomMember roomMember = invocation.getArgument(0);
            ReflectionTestUtils.setField(roomMember, "id", 1L);
            return roomMember;
        });

        given(chatRoomMemberRepository.findByChatRoomIdAndMemberId(anyLong(), anyLong()))
                .willReturn(Optional.of(MateChatRoomMember.builder()
                        .id(1L)
                        .mateChatRoom(MateChatRoom.builder().id(1L).build())
                        .member(member)
                        .isActive(true)
                        .build()));

        given(chatMessageRepository.findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(Page.empty());

        // when
        MateChatRoomResponse response = chatRoomService.createOrJoinChatRoomFromPost(postId, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomId()).isNotNull();
        assertThat(response.getMatePostId()).isEqualTo(postId);
        assertThat(response.getMemberId()).isEqualTo(memberId);
        verify(chatRoomRepository).save(any(MateChatRoom.class));
        verify(chatRoomMemberRepository).save(any(MateChatRoomMember.class));
    }

    @Test
    @DisplayName("메이트 게시글에서 채팅방 입장 성공 - 기존 채팅방이 있는 경우")
    void joinExistingChatRoom_Success() {
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Long roomId = 1L;
        Member member = createMember(memberId, "member1", 25, Gender.MALE);
        Member author = createMember(2L, "author", 25, Gender.MALE);
        MatePost matePost = createMatePost(postId, author);
        MateChatRoom existingRoom = createChatRoom(roomId, matePost);
        MateChatRoomMember chatRoomMember = createChatRoomMember(existingRoom.getId(), memberId, LocalDateTime.now());

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(chatRoomMember));
        when(chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(roomId)).thenReturn(2);
        when(chatMessageRepository.findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        // when
        MateChatRoomResponse response = chatRoomService.joinExistingChatRoom(roomId, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRoomId()).isEqualTo(roomId);
        assertThat(response.getMatePostId()).isEqualTo(postId);
        assertThat(response.getMemberId()).isEqualTo(memberId);
        verify(chatRoomMemberRepository, times(3)).findByChatRoomIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("채팅방 목록 조회 성공")
    void getMyChatRooms_Success() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId, "member1", 25, Gender.MALE);
        Pageable pageable = PageRequest.of(0, 10);
        List<MateChatRoom> chatRooms = List.of(
                createChatRoom(1L, createMatePost(1L, member)),
                createChatRoom(2L, createMatePost(2L, member))
        );
        Page<MateChatRoom> chatRoomPage = new PageImpl<>(chatRooms, pageable, chatRooms.size());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(chatRoomRepository.findActiveChatRoomsByMemberId(memberId, pageable))
                .willReturn(chatRoomPage);

        // when
        PageResponse<MateChatRoomListResponse> response = chatRoomService.getMyChatRooms(memberId, pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        verify(chatRoomRepository).findActiveChatRoomsByMemberId(memberId, pageable);
    }

    @Test
    @DisplayName("채팅방 나가기 성공")
    void leaveChatRoom_Success() {
        // given
        Long roomId = 1L;
        Long memberId = 1L;
        Member member = createMember(memberId, "member1", 25, Gender.MALE);
        Member author = createMember(2L, "author", 25, Gender.MALE);
        MatePost matePost = createMatePost(1L, author);
        MateChatRoom chatRoom = createChatRoom(roomId, matePost);
        MateChatRoomMember chatRoomMember = createChatRoomMember(roomId, memberId, LocalDateTime.now());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, memberId))
                .willReturn(Optional.of(chatRoomMember));
        given(chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(roomId)).willReturn(1);

        // when
        chatRoomService.leaveChatRoom(roomId, memberId);

        // then
        assertThat(chatRoomMember.getIsActive()).isFalse();
        verify(eventPublisher).publish(any(MateChatEvent.class));
    }

    @Test
    @DisplayName("성별 제한 위반 시 채팅방 입장 실패")
    void joinChatRoom_GenderRestrictionViolated() {
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Member member = createMember(memberId, "member1", 25, Gender.FEMALE);
        Member author = createMember(2L, "author", 25, Gender.MALE);
        MatePost matePost = createMatePost(postId, author, Age.ALL, Gender.MALE);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(mateRepository.findById(postId)).willReturn(Optional.of(matePost));

        // when & then
        assertThatThrownBy(() -> chatRoomService.createOrJoinChatRoomFromPost(postId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENDER_RESTRICTION_VIOLATED);

        verify(memberRepository).findById(memberId);
        verify(mateRepository).findById(postId);
        verifyNoInteractions(chatRoomRepository);
    }

    @Test
    @DisplayName("연령대 제한 위반 시 채팅방 입장 실패 - 20대 전용방에 30대가 입장 시도")
    void joinChatRoom_AgeRestrictionViolated_ThirtiesInTwentiesRoom() {
        // given
        Long memberId = 1L;
        Long postId = 1L;
        Member member = createMember(memberId, "member1", 35, Gender.MALE); // 35세 회원
        Member author = createMember(2L, "author", 25, Gender.MALE);
        MatePost matePost = createMatePost(postId, author, Age.TWENTIES, Gender.ANY); // 20대 전용방

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(mateRepository.findById(postId)).willReturn(Optional.of(matePost));

        // when & then
        assertThatThrownBy(() -> chatRoomService.createOrJoinChatRoomFromPost(postId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AGE_RESTRICTION_VIOLATED);

        verify(memberRepository).findById(memberId);
        verify(mateRepository).findById(postId);
        verifyNoInteractions(chatRoomRepository);
    }

    private Member createMember(Long id, String nickname, int age, Gender gender) {
        Member member = Member.builder()
                .nickname(nickname)
                .age(age)
                .gender(gender)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private MatePost createMatePost(Long id, Member author, Age age, Gender gender) {
        MatePost matePost = MatePost.builder()
                .author(author)
                .title("Test Post")
                .content("Test Content")
                .status(Status.OPEN)
                .age(age)
                .gender(gender)
                .maxParticipants(5)
                .build();
        ReflectionTestUtils.setField(matePost, "id", id);
        return matePost;
    }

    private MatePost createMatePost(Long id, Member author) {
        return createMatePost(id, author, Age.ALL, Gender.ANY);
    }

    private MateChatRoom createChatRoom(Long id, MatePost matePost) {
        MateChatRoom chatRoom = MateChatRoom.builder()
                .matePost(matePost)
                .isActive(true)
                .isMessageable(true)
                .isAuthorLeft(false)
                .currentMembers(1)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", id);
        return chatRoom;
    }

    private MateChatRoomMember createChatRoomMember(Long roomId, Long memberId, LocalDateTime lastEnteredAt) {
        MateChatRoomMember chatRoomMember = MateChatRoomMember.builder()
                .mateChatRoom(createChatRoom(roomId, createMatePost(1L, createMember(2L, "author", 25, Gender.MALE))))
                .member(createMember(memberId, "member" + memberId, 25, Gender.MALE))
                .isActive(true)
                .hasEntered(true)
                .lastEnteredAt(lastEnteredAt)
                .build();
        ReflectionTestUtils.setField(chatRoomMember, "id", 1L);
        return chatRoomMember;
    }
}