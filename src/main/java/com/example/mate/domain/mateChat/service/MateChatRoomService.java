package com.example.mate.domain.mateChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mateChat.dto.response.MateChatMessageResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MateChatRoomService {

    private final MateChatRoomRepository chatRoomRepository;
    private final MateChatRoomMemberRepository chatRoomMemberRepository;
    private final MateChatMessageRepository chatMessageRepository;
    private final MateRepository mateRepository;
    private final MemberRepository memberRepository;

    // 채팅방 생성 또는 입장
    public MateChatRoomResponse joinChatRoom(Long matePostId, Long memberId) {
        // 1. 사용자와 메이트 게시글 조회
        Member member = findMemberById(memberId);
        MatePost matePost = findMatePostById(matePostId);

        // 2. 입장 가능 여부 검증
        validateChatRoomJoin(matePost, member);

        // 3. 채팅방 조회 또는 생성
        MateChatRoom chatRoom = chatRoomRepository.findByMatePostId(matePostId)
                .orElseGet(() -> createChatRoom(matePost));

        // 4. 채팅방 멤버로 등록
        MateChatRoomMember chatRoomMember = joinAsMember(chatRoom, member);

        return MateChatRoomResponse.from(chatRoom, chatRoomMember);
    }

    // 채팅방 입장 가능 여부 검증
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
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        // 5. 이미 참여 중인지 검증
        MateChatRoom existingChatRoom = chatRoomRepository.findByMatePostId(matePost.getId()).orElse(null);
        if (existingChatRoom != null &&
                chatRoomMemberRepository.existsByChatRoomIdAndMemberId(existingChatRoom.getId(), member.getId())) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_CHAT_ROOM);
        }
    }

    // 채팅방 생성
    private MateChatRoom createChatRoom(MatePost matePost) {
        // 1. 채팅방 생성
        MateChatRoom mateChatRoom = MateChatRoom.builder()
                .matePost(matePost)
                .build();
        chatRoomRepository.save(mateChatRoom);

        // 2. 방장을 채팅방 멤버로 자동 등록
        MateChatRoomMember authorMember = MateChatRoomMember.builder()
                .mateChatRoom(mateChatRoom)
                .member(matePost.getAuthor())  // 게시글 작성자(방장)
                .build();
        chatRoomMemberRepository.save(authorMember);

        return chatRoomRepository.save(mateChatRoom);
    }

    // 채팅방 멤버 등록
    private MateChatRoomMember joinAsMember(MateChatRoom chatRoom, Member member) {
        // 1. 현재 채팅방 인원 검증
        int currentMembers = chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(chatRoom.getId());
        if (currentMembers >= 10) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FULL);
        }

        // 2. 멤버 등록 및 인원 증가
        MateChatRoomMember chatRoomMember = MateChatRoomMember.builder()
                .mateChatRoom(chatRoom)
                .member(member)
                .build();
        chatRoom.incrementCurrentMembers();

        return chatRoomMemberRepository.save(chatRoomMember);
    }

    // 채팅방 퇴장
    public void leaveChatRoom(Long roomId, Long memberId) {
        MateChatRoomMember chatRoomMember = findChatRoomMember(roomId, memberId);
        chatRoomMember.deactivate();

        // 채팅방 내 활성 멤버가 없으면 채팅방 비활성화
        int activeMembers = chatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(roomId);
        if (activeMembers == 0) {
            MateChatRoom chatRoom = chatRoomMember.getMateChatRoom();
            chatRoom.deactivate();
        }
    }

    // 채팅 내역 조회
    @Transactional(readOnly = true)
    public PageResponse<MateChatMessageResponse> getChatMessages(Long roomId, Pageable pageable) {
        MateChatRoom chatRoom = findMateChatRoomById(roomId);
        Page<MateChatMessage> messagePage = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageable);

        List<MateChatMessageResponse> content = messagePage.getContent().stream()
                .map(message -> MateChatMessageResponse.builder()
                        .type(message.getType())
                        .roomId(roomId)
                        .senderId(message.getSender().getId())
                        .senderNickname(message.getSender().getNickname())
                        .message(message.getContent())
                        .timestamp(message.getCreatedAt())
                        .currentMembers(chatRoom.getCurrentMembers())
                        .build())
                .toList();

        return PageResponse.from(messagePage, content);
    }


    // Utility 메소드들
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private MatePost findMatePostById(Long postId) {
        return mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.MATE_POST_NOT_FOUND_BY_ID));
    }

    private MateChatRoom findMateChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private MateChatRoomMember findChatRoomMember(Long roomId, Long memberId) {
        return chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));
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
