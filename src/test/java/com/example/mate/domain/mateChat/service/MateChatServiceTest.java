package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.matePost.entity.Status;
import com.example.mate.domain.matePost.entity.Visit;
import com.example.mate.domain.matePost.repository.MatePostRepository;
import com.example.mate.domain.matePost.repository.VisitPartRepository;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import com.example.mate.domain.mateChat.event.MateChatEvent;
import com.example.mate.domain.mateChat.event.MateChatEventPublisher;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomMemberRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MateChatServiceTest {

    @Mock
    private MateChatRoomRepository chatRoomRepository;
    @Mock
    private MateChatRoomMemberRepository chatRoomMemberRepository;
    @Mock
    private MateChatMessageRepository chatMessageRepository;
    @Mock
    private MatePostRepository matePostRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private VisitPartRepository visitPartRepository;
    @Mock
    private MateChatEventPublisher eventPublisher;

    @InjectMocks
    private MateChatRoomService chatRoomService;

    @Test
    @DisplayName("채팅방 생성/입장 - 신규 채팅방 생성")
    void createOrJoinChatRoomFromPost_CreateNew() {
        // Given
        Member testMember = createMember(1L, 25, Gender.MALE);
        Member authorMember = createMember(2L, 30, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.ANY, Status.OPEN);
        MateChatRoom chatRoom = createChatRoom(1L, matePost, true, true);
        MateChatRoomMember chatRoomMember = createChatRoomMember(1L, chatRoom, testMember, true, true);

        when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
        when(matePostRepository.findById(matePost.getId())).thenReturn(Optional.of(matePost));
        when(chatRoomRepository.findByMatePostId(matePost.getId())).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(MateChatRoom.class))).thenReturn(chatRoom);
        when(chatRoomMemberRepository.save(any(MateChatRoomMember.class))).thenReturn(chatRoomMember);

        // 이 부분이 validateChatRoomAccess에서 필요한 mock
        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(any(), any()))
                .thenReturn(Optional.of(chatRoomMember));

        when(chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(any()))
                .thenReturn(2);
        when(chatMessageRepository.findByRoomIdAndSendTimeAfter(any(), any(), any()))
                .thenReturn(Page.empty());

        // When
        MateChatRoomResponse response = chatRoomService.createOrJoinChatRoomFromPost(
                matePost.getId(),
                testMember.getId()
        );

        // Then
        assertNotNull(response);
        verify(chatRoomRepository).save(any(MateChatRoom.class));
        verify(chatRoomMemberRepository).save(any(MateChatRoomMember.class));
    }


    @Test
    @DisplayName("채팅방 입장 - 연령 제한에 걸리는 경우")
    void joinChatRoom_AgeRestrictionViolated() {
        // Given
        Member testMember = createMember(1L, 25, Gender.MALE);
        Member authorMember = createMember(2L, 40, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.FORTIES, Gender.ANY, Status.OPEN);

        when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
        when(matePostRepository.findById(matePost.getId())).thenReturn(Optional.of(matePost));

        // When & Then
        assertThrows(CustomException.class, () ->
                chatRoomService.createOrJoinChatRoomFromPost(matePost.getId(), testMember.getId())
        );
    }

    @Test
    @DisplayName("채팅방 입장 - 성별 제한에 걸리는 경우")
    void joinChatRoom_GenderRestrictionViolated() {
        // Given
        Member testMember = createMember(1L, 25, Gender.MALE);
        Member authorMember = createMember(2L, 25, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.FEMALE, Status.OPEN);

        when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
        when(matePostRepository.findById(matePost.getId())).thenReturn(Optional.of(matePost));

        // When & Then
        assertThrows(CustomException.class, () ->
                chatRoomService.createOrJoinChatRoomFromPost(matePost.getId(), testMember.getId())
        );
    }

    @Test
    @DisplayName("채팅방 퇴장 - 일반 멤버")
    void leaveChatRoom_NormalMember() {
        // Given
        Member testMember = createMember(1L, 25, Gender.MALE);
        Member authorMember = createMember(2L, 25, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.ANY, Status.OPEN);
        MateChatRoom chatRoom = createChatRoom(1L, matePost, true, true);
        MateChatRoomMember chatRoomMember = createChatRoomMember(1L, chatRoom, testMember, true, true);

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), testMember.getId()))
                .thenReturn(Optional.of(chatRoomMember));

        // When
        chatRoomService.leaveChatRoom(chatRoom.getId(), testMember.getId());

        // Then
        verify(eventPublisher).publish(any(MateChatEvent.class));
        assertFalse(chatRoomMember.getIsActive());
    }

    @Test
    @DisplayName("채팅방 퇴장 - 방장 퇴장 (직관 완료)")
    void leaveChatRoom_Author_VisitComplete() {
        // Given
        Member authorMember = createMember(2L, 25, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.ANY, Status.VISIT_COMPLETE);
        MateChatRoom chatRoom = createChatRoom(1L, matePost, true, true);
        MateChatRoomMember chatRoomMember = createChatRoomMember(1L, chatRoom, authorMember, true, true);

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findById(authorMember.getId())).thenReturn(Optional.of(authorMember));
        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), authorMember.getId()))
                .thenReturn(Optional.of(chatRoomMember));

        // When
        chatRoomService.leaveChatRoom(chatRoom.getId(), authorMember.getId());

        // Then
        verify(eventPublisher).publish(any(MateChatEvent.class));
        assertTrue(chatRoom.getIsAuthorLeft());
        assertFalse(chatRoom.getIsMessageable());
    }

    @Test
    @DisplayName("채팅방 퇴장 - 방장 퇴장 (직관 미완료)")
    void leaveChatRoom_Author_NotVisitComplete() {
        // Given
        Member authorMember = createMember(2L, 25, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.ANY, Status.OPEN);
        MateChatRoom chatRoom = createChatRoom(1L, matePost, true, true);
        MateChatRoomMember chatRoomMember = createChatRoomMember(1L, chatRoom, authorMember, true, true);

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findById(authorMember.getId())).thenReturn(Optional.of(authorMember));
        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), authorMember.getId()))
                .thenReturn(Optional.of(chatRoomMember));

        // When & Then
        assertThrows(CustomException.class, () ->
                chatRoomService.leaveChatRoom(chatRoom.getId(), authorMember.getId())
        );
    }

    @Test
    @DisplayName("채팅방 멤버 목록 조회")
    void getChatRoomMembers() {
        // Given
        Member testMember = createMember(1L, 25, Gender.MALE);
        Member authorMember = createMember(2L, 25, Gender.FEMALE);
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.ANY, Status.OPEN);
        MateChatRoom chatRoom = createChatRoom(1L, matePost, true, true);
        MateChatRoomMember chatRoomMember = createChatRoomMember(1L, chatRoom, testMember, true, true);

        List<MateChatRoomMember> activeMembers = List.of(chatRoomMember);
        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), testMember.getId()))
                .thenReturn(Optional.of(chatRoomMember));
        when(chatRoomMemberRepository.findActiveMembers(chatRoom.getId())).thenReturn(activeMembers);

        // When
        List<MemberSummaryResponse> response = chatRoomService.getChatRoomMembers(
                chatRoom.getId(),
                testMember.getId()
        );

        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
        verify(chatRoomMemberRepository).findActiveMembers(chatRoom.getId());
    }

    @Test
    @DisplayName("직관 완료 상태에서 비참여자 채팅방 접근 제한")
    void joinChatRoom_VisitComplete_AccessDenied() {
        // Given
        Member testMember = createMember(1L, 25, Gender.MALE);
        Member authorMember = createMember(2L, 25, Gender.FEMALE);
        Visit visit = Visit.builder().id(1L).build();
        MatePost matePost = createMatePost(1L, authorMember, Age.ALL, Gender.ANY, Status.VISIT_COMPLETE, visit);

        when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
        when(matePostRepository.findById(matePost.getId())).thenReturn(Optional.of(matePost));
        when(visitPartRepository.existsByVisitAndMember(visit.getId(), testMember.getId()))
                .thenReturn(false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                chatRoomService.createOrJoinChatRoomFromPost(matePost.getId(), testMember.getId())
        );

        assertEquals(ErrorCode.CHAT_ROOM_ACCESS_DENIED, exception.getErrorCode());
    }

    private Member createMember(Long id, int age, Gender gender) {
        return Member.builder()
                .id(id)
                .age(age)
                .gender(gender)
                .nickname("nickname" + id)
                .build();
    }

    private MatePost createMatePost(Long id, Member author, Age age, Gender gender, Status status, Visit visit) {
        return MatePost.builder()
                .id(id)
                .author(author)
                .age(age)
                .gender(gender)
                .status(status)
                .title("Test Post")
                .content("Test Content")
                .visit(visit)
                .build();
    }

    private MatePost createMatePost(Long id, Member author, Age age, Gender gender, Status status) {
        return createMatePost(id, author, age, gender, status, null);
    }

    private MateChatRoom createChatRoom(Long id, MatePost matePost, boolean isActive, boolean isMessageable) {
        // members 리스트 초기화 추가
        return MateChatRoom.builder()
                .id(id)
                .matePost(matePost)
                .currentMembers(1)
                .isActive(isActive)
                .isMessageable(isMessageable)
                .isAuthorLeft(false)
                .members(new ArrayList<>())
                .build();
    }

    private MateChatRoomMember createChatRoomMember(Long id, MateChatRoom chatRoom, Member member,
                                                    boolean isActive, boolean hasEntered) {
        return MateChatRoomMember.builder()
                .id(id)
                .mateChatRoom(chatRoom)
                .member(member)
                .isActive(isActive)
                .hasEntered(hasEntered)
                .lastEnteredAt(LocalDateTime.now())
                .build();
    }
}