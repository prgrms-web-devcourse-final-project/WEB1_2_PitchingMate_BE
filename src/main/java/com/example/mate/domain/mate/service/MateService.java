package com.example.mate.domain.mate.service;

import static com.example.mate.common.error.ErrorCode.ALREADY_COMPLETED_POST;
import static com.example.mate.common.error.ErrorCode.INVALID_MATE_POST_PARTICIPANT_IDS;
import static com.example.mate.common.error.ErrorCode.MATCH_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MATE_POST_COMPLETE_TIME_NOT_ALLOWED;
import static com.example.mate.common.error.ErrorCode.MATE_POST_MAX_PARTICIPANTS_EXCEEDED;
import static com.example.mate.common.error.ErrorCode.MATE_POST_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MATE_POST_UPDATE_NOT_ALLOWED;
import static com.example.mate.common.error.ErrorCode.MEMBER_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.NOT_CLOSED_STATUS_FOR_COMPLETION;
import static com.example.mate.common.error.ErrorCode.NOT_PARTICIPANT_OR_AUTHOR;
import static com.example.mate.common.error.ErrorCode.TEAM_NOT_FOUND;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileService;
import com.example.mate.domain.file.FileValidator;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.MatePostCompleteRequest;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.request.MatePostSearchRequest;
import com.example.mate.domain.mate.dto.request.MatePostStatusRequest;
import com.example.mate.domain.mate.dto.request.MatePostUpdateRequest;
import com.example.mate.domain.mate.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.mate.dto.response.MatePostCompleteResponse;
import com.example.mate.domain.mate.dto.response.MatePostDetailResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.mate.dto.response.MateReviewCreateResponse;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.Visit;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomMemberRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class MateService {

    private static final String DEFAULT_MATE_POST_IMAGE = "mate_default.svg";

    private final MateRepository mateRepository;
    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;
    private final MateReviewRepository mateReviewRepository;
    private final MateChatRoomRepository mateChatRoomRepository;
    private final MateChatRoomMemberRepository mateChatRoomMemberRepository;
    private final FileService fileService;

    public MatePostResponse createMatePost(MatePostCreateRequest request, MultipartFile file, Long memberId) {
        Member author = findMemberById(memberId);

        Match match = findMatchById(request.getMatchId());

        validateTeamId(request.getTeamId());

        MatePost matePost = MatePost.builder()
                .author(author)
                .teamId(request.getTeamId())
                .match(match)
                .imageUrl(DEFAULT_MATE_POST_IMAGE)
                .title(request.getTitle())
                .content(request.getContent())
                .status(Status.OPEN)
                .maxParticipants(request.getMaxParticipants())
                .age(request.getAge())
                .gender(request.getGender())
                .transport(request.getTransportType())
                .build();
        MatePost savedPost = mateRepository.save(matePost);

        handleFileUpload(file, matePost);

        return MatePostResponse.from(savedPost);
    }

    private void handleFileUpload(MultipartFile file, MatePost matePost) {
        if (file != null && !file.isEmpty()) {
            FileValidator.validateSingleImage(file);
            String imageUrl = fileService.uploadImageWithThumbnail(file);
            matePost.changeImageUrl(imageUrl);
        }
    }

    @Transactional(readOnly = true)
    public List<MatePostSummaryResponse> getMainPagePosts(Long teamId) {
        if (teamId != null && !TeamInfo.existById(teamId)) {
            throw new CustomException(TEAM_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Status> validStatuses = List.of(Status.OPEN, Status.CLOSED);

        List<MatePost> mainPagePosts = mateRepository.findMainPagePosts(
                teamId,
                now,
                validStatuses,
                PageRequest.of(0, 3));

        return mainPagePosts.stream()
                .map(MatePostSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MatePostSummaryResponse> getMatePagePosts(MatePostSearchRequest request, Pageable pageable) {
        if (request.getTeamId() != null && !TeamInfo.existById(request.getTeamId())) {
            throw new CustomException(TEAM_NOT_FOUND);
        }

        Page<MatePost> matePostPage = mateRepository.findMatePostsByFilter(request, pageable);

        List<MatePostSummaryResponse> content = matePostPage.getContent().stream()
                .map(MatePostSummaryResponse::from)
                .toList();

        return PageResponse.from(matePostPage, content);
    }

    @Transactional(readOnly = true)
    public MatePostDetailResponse getMatePostDetail(Long postId) {
        MatePost matePost = findMatePostById(postId);

        Integer currentChatMembers = getCurrentChatMembers(postId);

        return MatePostDetailResponse.from(matePost, currentChatMembers);
    }

    private Integer getCurrentChatMembers(Long postId) {
        return mateChatRoomRepository.findByMatePostId(postId)
                .map(chatRoom -> mateChatRoomMemberRepository.countByChatRoomIdAndIsActiveTrue(chatRoom.getId()))
                .orElse(0);
    }

    public MatePostResponse updateMatePost(Long memberId, Long postId, MatePostUpdateRequest request,
                                           MultipartFile file) {
        MatePost matePost = findMatePostById(postId);
        validateAuthorization(matePost, memberId);
        validatePostStatus(matePost.getStatus());
        validateTeamId(request.getTeamId());

        Match match = findMatchById(request.getMatchId());
        String imageUrl = updateImage(matePost.getImageUrl(), file);

        matePost.updatePost(request, match, imageUrl);

        return MatePostResponse.from(matePost);
    }

    private void validateTeamId(Long teamId) {
        if (!TeamInfo.existById(teamId)) {
            throw new CustomException(TEAM_NOT_FOUND);
        }
    }

    private Match findMatchById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(MATCH_NOT_FOUND_BY_ID));
    }

    private String updateImage(String currentImageUrl, MultipartFile newFile) {
        if (newFile == null || newFile.isEmpty()) {
            return currentImageUrl;
        }

        FileValidator.validateSingleImage(newFile);
        deleteNonDefaultImage(currentImageUrl);

        return fileService.uploadImageWithThumbnail(newFile);
    }

    private void deleteNonDefaultImage(String imageUrl) {
        if (!imageUrl.equals(DEFAULT_MATE_POST_IMAGE)) {
            fileService.deleteFile(imageUrl);
        }
    }

    public MatePostResponse updateMatePostStatus(Long memberId, Long postId, MatePostStatusRequest request) {
        MatePost matePost = findMatePostById(postId);

        validateAuthorization(matePost, memberId);
        validatePostStatus(matePost.getStatus());

        if (request.getStatus() == Status.CLOSED) {
            findAndValidateParticipants(request.getParticipantIds(), matePost.getMaxParticipants());
        }
        matePost.changeStatus(request.getStatus());

        return MatePostResponse.from(matePost);
    }

    public void deleteMatePost(Long memberId, Long postId) {
        MatePost matePost = findMatePostById(postId);

        validateAuthorization(matePost, memberId);
        validatePostStatus(matePost.getStatus());
        deleteNonDefaultImage(matePost.getImageUrl());

        mateRepository.delete(matePost);
    }

    private void validatePostStatus(Status status) {
        if (status == Status.VISIT_COMPLETE) {
            throw new CustomException(ALREADY_COMPLETED_POST);
        }
    }

    public MatePostCompleteResponse completeVisit(Long memberId, Long postId, MatePostCompleteRequest request) {
        MatePost matePost = findMatePostById(postId);
        validateAuthorization(matePost, memberId);

        validateCompletionTime(matePost);
        validateCompletionStatus(matePost);

        List<Member> participants = findAndValidateParticipants(request.getParticipantIds(),
                matePost.getMaxParticipants());

        matePost.complete(participants);
        return MatePostCompleteResponse.from(matePost);
    }

    private void validateAuthorization(MatePost matePost, Long memberId) {
        if (!matePost.getAuthor().getId().equals(memberId)) {
            throw new CustomException(MATE_POST_UPDATE_NOT_ALLOWED);
        }
    }

    private void validateCompletionTime(MatePost matePost) {
        if (matePost.getMatch().getMatchTime().isAfter(LocalDateTime.now())) {
            throw new CustomException(MATE_POST_COMPLETE_TIME_NOT_ALLOWED);
        }
    }

    private void validateCompletionStatus(MatePost matePost) {
        if (matePost.getStatus() != Status.CLOSED) {
            throw new CustomException(NOT_CLOSED_STATUS_FOR_COMPLETION);
        }
    }

    private List<Member> findAndValidateParticipants(List<Long> participantIds, int maxParticipants) {
        List<Member> participants = memberRepository.findAllById(participantIds);
        validateParticipantExistence(participantIds.size(), participants.size());
        validateParticipantCount(participants.size(), maxParticipants);
        return participants;
    }

    private void validateParticipantExistence(int requestedCount, int foundCount) {
        if (foundCount != requestedCount) {
            throw new CustomException(INVALID_MATE_POST_PARTICIPANT_IDS);
        }
    }

    private void validateParticipantCount(int participantCount, int maxParticipants) {
        int totalParticipantCount = participantCount + 1;
        if (totalParticipantCount > maxParticipants) {
            throw new CustomException(MATE_POST_MAX_PARTICIPANTS_EXCEEDED);
        }
    }

    public MateReviewCreateResponse createReview(Long postId, Long reviewerId, MateReviewCreateRequest request) {
        MatePost matePost = findMatePostById(postId);
        Member reviewer = findMemberById(reviewerId);
        Member reviewee = findMemberById(request.getRevieweeId());

        validateReviewEligibility(matePost, reviewer, reviewee);

        MateReview review = matePost.getVisit().createReview(reviewer, reviewee, request);
        MateReview savedReview = mateReviewRepository.save(review);
        reviewee.updateManner(request.getRating());
        memberRepository.save(reviewee);

        return MateReviewCreateResponse.from(savedReview);
    }

    private void validateReviewEligibility(MatePost matePost, Member reviewer, Member reviewee) {
        // 리뷰어와 리뷰 대상자 모두 참여자(또는 방장) 여부 검증
        validateParticipant(matePost, reviewer);
        validateParticipant(matePost, reviewee);

        if (matePost.getStatus() != Status.VISIT_COMPLETE) {
            throw new CustomException(ErrorCode.MATE_REVIEW_STATUS_NOT_VISIT_COMPLETE);
        }

        if (mateReviewRepository.existsByVisitIdAndReviewerIdAndRevieweeId(
                matePost.getVisit().getId(),
                reviewer.getId(),
                reviewee.getId())) {
            throw new CustomException(ErrorCode.MATE_REVIEW_ALREADY_EXISTS);
        }
    }

    private void validateParticipant(MatePost matePost, Member member) {
        boolean isParticipant = isAuthor(matePost, member) ||
                isVisitParticipant(matePost.getVisit(), member);

        if (!isParticipant) {
            throw new CustomException(NOT_PARTICIPANT_OR_AUTHOR);
        }
    }

    private boolean isAuthor(MatePost matePost, Member member) {
        return matePost.getAuthor().equals(member);
    }

    private boolean isVisitParticipant(Visit visit, Member member) {
        return visit.getParticipants().stream()
                .anyMatch(part -> part.getMember().equals(member));
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND_BY_ID));
    }

    private MatePost findMatePostById(Long postId) {
        return mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(MATE_POST_NOT_FOUND_BY_ID));
    }
}
