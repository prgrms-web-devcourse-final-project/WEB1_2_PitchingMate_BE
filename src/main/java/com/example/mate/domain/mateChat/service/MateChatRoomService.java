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

import java.util.Collections;
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

    // 채팅방 생성 또는 입장
    public MateChatRoomResponse createOrJoinChatRoom(Long matePostId, Long memberId) {
        // 1. 사용자와 메이트 게시글 조회
        Member member = findMemberById(memberId);
        MatePost matePost = findMatePostById(matePostId);

        // 2. 입장 가능 여부 검증
        validateChatRoomJoin(matePost, member);

        // 3. 채팅방 생성 또는 조회
        MateChatRoom chatRoom = chatRoomRepository.findByMatePostId(matePostId)
                .orElseGet(() -> createChatRoom(matePost));

        // 4. 채팅방 멤버로 등록 또는 조회
        MateChatRoomMember chatRoomMember = joinAsMember(chatRoom, member);

        // 5. 초기 메시지 처리
        PageResponse<MateChatMessageResponse> initialMessages;
        if (chatRoomMember.markAsEntered()) {
            initialMessages = createEmptyMessagePage();
            sendEnterMessage(chatRoom.getId(), member);
        } else {
            initialMessages = getChatMessages(chatRoom.getId(), PageRequest.of(0, 20));
        }

        // 6. 최초 입장인 경우 입장 메시지 전송
        if (chatRoomMember.markAsEntered()) {
            sendEnterMessage(chatRoom.getId(), member);
        }

        return MateChatRoomResponse.from(chatRoom, chatRoomMember, initialMessages);
    }

    // 채팅방 메시지 내역 조회
    @Transactional(readOnly = true)
    public PageResponse<MateChatMessageResponse> getChatMessages(Long roomId, Pageable pageable) {
        validateChatRoom(roomId);

        Page<MateChatMessage> messagePage = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(roomId, pageable);

        List<MateChatMessageResponse> content = messagePage.getContent().stream()
                .map(MateChatMessageResponse::of)
                .toList();

        return PageResponse.from(messagePage, content);
    }

    // 내 채팅방 목록 조회
    @Transactional(readOnly = true)
    public PageResponse<MateChatRoomListResponse> getMyChatRooms(Long memberId, Pageable pageable) {
        validateMember(memberId);

        Page<MateChatRoom> chatRooms = chatRoomRepository
                .findActiveChatRoomsByMemberId(memberId, pageable);

        List<MateChatRoomListResponse> content = chatRooms.getContent().stream()
                .map(MateChatRoomListResponse::from)
                .toList();

        return PageResponse.from(chatRooms, content);
    }

    // 채팅방 퇴장 처리
    public void leaveChatRoom(Long roomId, Long memberId) {
        MateChatRoomMember chatRoomMember = findChatRoomMember(roomId, memberId);

        // 퇴장 처리 및 메시지 전송
        chatRoomMember.deactivate();
        sendLeaveMessage(roomId, findMemberById(memberId));

        // 채팅방 내 활성 멤버가 없으면 채팅방 비활성화
        if (chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(roomId) == 0) {
            MateChatRoom chatRoom = chatRoomMember.getMateChatRoom();
            chatRoom.deactivate();
        }
    }

    // 유틸 메서드들
    private void validateChatRoomJoin(MatePost matePost, Member member) {
        // 1. 작성자 검증 (작성자는 참여 불가)
        if (matePost.getAuthor().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.AUTHOR_CANNOT_JOIN_CHAT);
        }

        // 2. 연령대 제한 검증
        if (matePost.getAge() != Age.ALL && !isAgeEligible(matePost.getAge(), member.getAge())) {
            throw new CustomException(ErrorCode.AGE_RESTRICTION_VIOLATED);
        }

        // 3. 성별 제한 검증
        if (matePost.getGender() != Gender.ANY && member.getGender() != matePost.getGender()) {
            throw new CustomException(ErrorCode.GENDER_RESTRICTION_VIOLATED);
        }

        // 4. 채팅방 상태 검증
        if (matePost.getStatus() == Status.VISIT_COMPLETE) {
            // 직관 완료 상태일 경우, 직관 참여자인지 확인
            boolean isVisitParticipant = visitPartRepository.existsByVisitAndMember(
                    matePost.getVisit(), member);
            boolean isAuthor = matePost.getAuthor().getId().equals(member.getId());

            if (!isVisitParticipant && !isAuthor) {
                throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
            }
        }

        // 5. 이미 참여 중인지 검증
        MateChatRoom existingChatRoom = chatRoomRepository.findByMatePostId(matePost.getId()).orElse(null);
        if (existingChatRoom != null &&
                chatRoomMemberRepository.existsByChatRoomIdAndMemberId(existingChatRoom.getId(), member.getId())) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_CHAT_ROOM);
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
            return existingMember.get();
        }

        // 신규 멤버인 경우에만 인원 검증 및 멤버 등록 수행
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

    private PageResponse<MateChatMessageResponse> createEmptyMessagePage() {
        return PageResponse.<MateChatMessageResponse>builder()
                .content(Collections.emptyList())
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .pageNumber(0)
                .pageSize(0)
                .build();
    }

    private void sendEnterMessage(Long roomId, Member member) {
        MateChatMessageRequest enterMessage = MateChatMessageRequest.createEnterMessage(
                roomId,
                member.getId(),
                member.getNickname()
        );
        mateChatMessageService.sendEnterMessage(enterMessage);
    }

    private void sendLeaveMessage(Long roomId, Member member) {
        MateChatMessageRequest leaveMessage = MateChatMessageRequest.createLeaveMessage(
                roomId,
                member.getId(),
                member.getNickname()
        );
        mateChatMessageService.sendLeaveMessage(leaveMessage);
    }

    private boolean isAgeEligible(Age requiredAge, int memberAge) {
        return switch (requiredAge) {
            case TEENS -> memberAge >= 10 && memberAge < 20;
            case TWENTIES -> memberAge >= 20 && memberAge < 30;
            case THIRTIES -> memberAge >= 30 && memberAge < 40;
            case FORTIES -> memberAge >= 40 && memberAge < 50;
            case OVER_FIFTIES -> memberAge >= 50;
            case ALL -> true;
        };
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private MatePost findMatePostById(Long postId) {
        return mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATE_POST_NOT_FOUND_BY_ID));
    }

    private void validateChatRoom(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }
    }

    private void validateMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID);
        }
    }

    private MateChatRoomMember findChatRoomMember(Long roomId, Long memberId) {
        return chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
    }
}