package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mateChat.document.MateChatMessage;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomListResponse;
import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import com.example.mate.domain.mateChat.event.MateChatEvent;
import com.example.mate.domain.mateChat.event.MateChatEventPublisher;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.mateChat.repository.MateChatMessageRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomMemberRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.matePost.entity.Status;
import com.example.mate.domain.matePost.repository.MatePostRepository;
import com.example.mate.domain.matePost.repository.VisitPartRepository;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.mate.common.error.ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class MateChatRoomService {
    private final MateChatRoomRepository chatRoomRepository;
    private final MateChatRoomMemberRepository chatRoomMemberRepository;
    private final MateChatMessageRepository chatMessageRepository;
    private final MatePostRepository matePostRepository;
    private final MemberRepository memberRepository;
    private final VisitPartRepository visitPartRepository;
    private final MateChatEventPublisher eventPublisher;

    // 메이트 게시글에서 채팅방 생성/입장
    public MateChatRoomResponse createOrJoinChatRoomFromPost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        MatePost matePost = matePostRepository.findById(postId)
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
        // 최초 입장이거나, 이전에 나갔다가 다시 들어온 경우
        if (!chatRoomMember.getHasEntered() || !chatRoomMember.getIsActive()) {
            chatRoomMember.markAsEntered();
            if (!chatRoomMember.getIsActive()) {
                chatRoomMember.activate();
                chatRoom.incrementCurrentMembers();
            }
            sendEnterMessage(chatRoom.getId(), member);
        }

        // 3. 채팅 가능 상태 업데이트
        int activeMembers = chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(chatRoom.getId());
        if (activeMembers >= 2) {
            chatRoom.setMessageable(true);
        }

        // 4. 메시지 조회
        List<MateChatMessageResponse> initialMessages =
                getChatMessages(chatRoom.getId(), member.getId(), chatRoomMember.getLastEnteredAt());

        return MateChatRoomResponse.from(chatRoom, chatRoomMember, initialMessages);
    }

    private void validateChatRoomJoin(MatePost matePost, Member member) {
        // 방장인 경우, 직관 완료 상태가 아닐 때만 입장 가능
        if (matePost.getAuthor().getId().equals(member.getId())) {
            if (matePost.getStatus() == Status.VISIT_COMPLETE) {
                throw new CustomException(ErrorCode.CHAT_AUTHOR_JOIN_DENIED);
            }
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
        if (matePost.getStatus() == Status.VISIT_COMPLETE && matePost.getVisit() != null) {
            boolean isVisitParticipant = visitPartRepository.existsByVisitAndMember(
                    matePost.getVisit().getId(),  // Visit 엔티티 대신 ID 전달
                    member.getId()                 // Member 엔티티 대신 ID 전달
            );

            if (!isVisitParticipant) {
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
            return existingMember.get();
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

    // 채팅방 현재 명단 조회
    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getChatRoomMembers(Long roomId, Long memberId) {

        // 1. 채팅방 접근 권한 검증
        chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

        // 2. 활성화된 채팅방 멤버 조회
        List<MateChatRoomMember> activeMembers = chatRoomMemberRepository.findActiveMembers(roomId);

        // 3. MemberSummaryResponse로 변환하여 반환
        return activeMembers.stream()
                .map(member -> MemberSummaryResponse.from(member.getMember()))
                .collect(Collectors.toList());
    }

    // 채팅방 퇴장
    public void leaveChatRoom(Long roomId, Long memberId) {
        MateChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        MateChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));


        // 방장 퇴장 시 검증
        if (isAuthor(chatRoom, member)) {
            validateAuthorLeave(chatRoom.getMatePost());
        }

        // 퇴장 처리
        chatRoomMember.deactivate();
        sendLeaveMessage(roomId, member);

        // 방장 퇴장 시 채팅방 상태 변경
        if (isAuthor(chatRoom, member)) {
            chatRoom.setAuthorLeft(true);
            List<MateChatRoomMember> remainingMembers = chatRoomMemberRepository.findActiveMembers(roomId);
            remainingMembers.forEach(m -> m.getMateChatRoom().setMessageable(false));
        }

        // 채팅방 상태 업데이트
        updateChatRoomStatus(chatRoom);
    }

    private void validateAuthorLeave(MatePost matePost) {
        if (matePost.getStatus() != Status.VISIT_COMPLETE) {
            throw new CustomException(ErrorCode.AUTHOR_LEAVE_NOT_ALLOWED);
        }
    }

    private void updateChatRoomStatus(MateChatRoom chatRoom) {
        // 현재 채팅방의 활성화된(아직 퇴장하지 않은) 멤버 수를 조회
        int activeMembers = chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(chatRoom.getId());

        // 활성화된 멤버가 0명인 경우
        if (activeMembers == 0) {
            // 채팅방을 비활성화 처리
            // - isActive를 false로 설정
            // - isMessageable을 false로 설정
            chatRoom.deactivate();

            // 활성화된 멤버가 1명인 경우
        } else if (activeMembers == 1) {
            // 채팅방은 활성화 상태로 유지하되, 메시지 전송은 불가능하도록 설정
            // 채팅방 목록에서는 조회되지만 메시지를 보낼 수는 없는 상태가 됨
            chatRoom.setMessageable(false);
        }
        // 활성화된 멤버가 2명 이상인 경우는 정상적으로 채팅방 유지
    }

    // 채팅 메세지 조회
    @Transactional(readOnly = true)
    public List<MateChatMessageResponse> getChatMessages(Long chatRoomId, Long senderId, LocalDateTime lastSentAt) {
        MateChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoomId, senderId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_MEMBER_NOT_FOUND));

        List<MateChatMessage> messageList = chatMessageRepository.getChatMessages(
                chatRoomId,
                chatRoomMember.getLastEnteredAt(),
                lastSentAt
        );

        // 발신자 ID 수집 및 조회
        Set<Long> senderIds = messageList.stream()
                .map(MateChatMessage::getSenderId)
                .collect(Collectors.toSet());

        Map<Long, Member> senderMap = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        // senderMap을 사용하여 메시지 변환
        return messageList.stream()
                .map(message -> {
                    Member sender = senderMap.get(message.getSenderId());
                    return MateChatMessageResponse.from(message, sender);
                })
                .toList();
    }

    // 내 채팅방 목록 조회
    @Transactional(readOnly = true)
    public PageResponse<MateChatRoomListResponse> getMyChatRooms(Long memberId, Pageable pageable) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        Page<MateChatRoom> chatRooms = chatRoomRepository.findActiveChatRoomsByMemberId(memberId, pageable);

        List<MateChatRoomListResponse> responses = chatRooms.getContent().stream()
                .map(room -> {
                    boolean isAuthor = room.getMatePost().getAuthor().getId().equals(memberId);
                    return MateChatRoomListResponse.from(room, isAuthor);
                })
                .toList();

        return PageResponse.from(chatRooms, responses);
    }

    private void sendEnterMessage(Long roomId, Member member) {
        eventPublisher.publish(MateChatEvent.from(roomId, member, MessageType.ENTER));
    }

    private void sendLeaveMessage(Long roomId, Member member) {
        eventPublisher.publish(MateChatEvent.from(roomId, member, MessageType.LEAVE));
    }

    private boolean isAuthor(MateChatRoom chatRoom, Member member) {
        return chatRoom.getMatePost().getAuthor().getId().equals(member.getId());
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
}