package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomMemberRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MateChatRoomService {
    private final MateChatRoomRepository chatRoomRepository;
    private final MateChatRoomMemberRepository chatRoomMemberRepository;
    private final MateChatMessageRepository chatMessageRepository;
    private final MateRepository mateRepository;
    private final MemberRepository memberRepository;
    private final MateChatMessageService mateChatMessageService;
    private final VisitPartRepository visitPartRepository;

    // 메이트 게시글에서 채팅방 생성/입장
    public MateChatRoomResponse createOrJoinChatRoomFromPost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        MatePost matePost = mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATE_POST_NOT_FOUND_BY_ID));

        // 1. 입장 가능 여부 검증
        validateChatRoomJoin(matePost, member);

        // 2. 채팅방 생성 또는 조회
        MateChatRoom chatRoom = chatRoomRepository.findByMatePostId(postId)
                .orElseGet(() -> createChatRoom(matePost));

        return processChatRoomJoin(chatRoom, member);
    }

    // 채팅방 목록에서 기존 채팅방 입장
    public MateChatRoomResponse joinExistingChatRoom(Long roomId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        MateChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 1. 입장 가능 여부 검증
        validateChatRoomJoin(chatRoom.getMatePost(), member);

        return processChatRoomJoin(chatRoom, member);
    }

    // 채팅방 입장 공통 로직
    private MateChatRoomResponse processChatRoomJoin(MateChatRoom chatRoom, Member member) {
        // 1. 채팅방 멤버로 등록 또는 조회
        MateChatRoomMember chatRoomMember = joinAsMember(chatRoom, member);

        // 2. 입장 체크 및 메시지 전송
        // 최초 입장이거나 (hasEntered가 false) 이전에 나갔다가 다시 들어온 경우 (isActive가 false였다가 true가 된 경우)
        boolean wasInactive = !chatRoomMember.getIsActive();
        if (!chatRoomMember.getHasEntered() || wasInactive) {
            chatRoomMember.markAsEntered();
            sendEnterMessage(chatRoom.getId(), member);
        }

        // 3. 채팅 가능 상태 업데이트
        int activeMembers = chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(chatRoom.getId());
        if (activeMembers >= 2) {
            chatRoom.setMessageable(true);
        }

        // 4. 메시지 조회
        PageResponse<MateChatMessageResponse> initialMessages =
                getChatMessages(chatRoom.getId(), member.getId(), PageRequest.of(0, 20));

        return MateChatRoomResponse.from(chatRoom, chatRoomMember, initialMessages);
    }

    private void validateChatRoomJoin(MatePost matePost, Member member) {
        // 방장인 경우 모든 제한을 건너뜀
        if (matePost.getAuthor().getId().equals(member.getId())) {
            return;
        }

        // 방장이 아닌 경우에만 연령대/성별 제한 검증
        // 1. 연령대 제한 검증
        if (matePost.getAge() != Age.ALL && !isAgeEligible(matePost.getAge(), member.getAge())) {
            throw new CustomException(ErrorCode.AGE_RESTRICTION_VIOLATED);
        }

        // 2. 성별 제한 검증
        if (matePost.getGender() != Gender.ANY && member.getGender() != matePost.getGender()) {
            throw new CustomException(ErrorCode.GENDER_RESTRICTION_VIOLATED);
        }

        // 3. 직관 완료 상태인 경우 접근 권한 검증
        if (matePost.getStatus() == Status.VISIT_COMPLETE) {
            boolean isVisitParticipant = visitPartRepository.existsByVisitAndMember(
                    matePost.getVisit(), member);
            boolean isAuthor = matePost.getAuthor().getId().equals(member.getId());

            if (!isVisitParticipant && !isAuthor) {
                throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        }
    }

    private MateChatRoom createChatRoom(MatePost matePost) {
        // 1. 채팅방 생성
        MateChatRoom mateChatRoom = MateChatRoom.builder()
                .matePost(matePost)
                .build();
        chatRoomRepository.save(mateChatRoom);

        // 2. 방장을 채팅방 멤버로 자동 등록
        MateChatRoomMember authorMember = MateChatRoomMember.builder()
                .mateChatRoom(mateChatRoom)
                .member(matePost.getAuthor())
                .hasEntered(true)  // 방장은 입장 처리된 것으로 간주
                .lastEnteredAt(LocalDateTime.now())
                .build();
        chatRoomMemberRepository.save(authorMember);

        return mateChatRoom;
    }

    private MateChatRoomMember joinAsMember(MateChatRoom chatRoom, Member member) {
        // 기존 채팅방 멤버 조회
        Optional<MateChatRoomMember> existingMember = chatRoomMemberRepository
                .findByChatRoomIdAndMemberId(chatRoom.getId(), member.getId());

        // 이미 존재하는 멤버라면 해당 멤버 정보 반환
        if (existingMember.isPresent()) {
            MateChatRoomMember chatRoomMember = existingMember.get();
            // 비활성 상태였다면 다시 활성화
            if (!chatRoomMember.getIsActive()) {
                chatRoomMember.activate();
                chatRoom.incrementCurrentMembers();
            }
            return chatRoomMember;
        }

        // 신규 멤버인 경우 인원 검증 및 멤버 등록 (기존 코드)
        int currentMembers = chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(chatRoom.getId());
        if (currentMembers >= 10) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FULL);
        }

        MateChatRoomMember chatRoomMember = MateChatRoomMember.builder()
                .mateChatRoom(chatRoom)
                .member(member)
                .build();
        chatRoom.incrementCurrentMembers();

        return chatRoomMemberRepository.save(chatRoomMember);
    }

}