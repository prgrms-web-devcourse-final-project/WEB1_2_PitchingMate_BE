package com.example.mate.domain.mate.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.utils.file.FileUploader;
import com.example.mate.common.utils.file.FileValidator;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.*;
import com.example.mate.domain.mate.dto.response.*;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.Visit;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.mate.common.error.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MateService {

    private final MateRepository mateRepository;
    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;
    private final MateReviewRepository mateReviewRepository;

    public MatePostResponse createMatePost(MatePostCreateRequest request, MultipartFile file) {
        Member author = findMemberById(request.getMemberId());

        Match match = findMatchById(request.getMatchId());

        validateTeamId(request.getTeamId());

        MatePost matePost = MatePost.builder()
                .author(author)
                .teamId(request.getTeamId())
                .match(match)
                .imageUrl(null) //TODO - image 서비스 배포 후 구현
                .title(request.getTitle())
                .content(request.getContent())
                .status(Status.OPEN)
                .maxParticipants(request.getMaxParticipants())
                .age(request.getAge())
                .gender(request.getGender())
                .transport(request.getTransportType())
                .build();
        MatePost savedPost = mateRepository.save(matePost);

        return MatePostResponse.from(savedPost);
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
        if (request.getTeamId()!= null && !TeamInfo.existById(request.getTeamId())) {
            throw new CustomException(TEAM_NOT_FOUND);
        }

        Page<MatePost> matePostPage = mateRepository.findMatePostsByFilter(request ,pageable);

        List<MatePostSummaryResponse> content = matePostPage.getContent().stream()
                .map(MatePostSummaryResponse::from)
                .toList();

        return PageResponse.<MatePostSummaryResponse>builder()
                .content(content)
                .totalPages(matePostPage.getTotalPages())
                .totalElements(matePostPage.getTotalElements())
                .hasNext(matePostPage.hasNext())
                .pageNumber(matePostPage.getNumber())
                .pageSize(matePostPage.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public MatePostDetailResponse getMatePostDetail(Long postId) {
        MatePost matePost = findMatePostById(postId);

        return MatePostDetailResponse.from(matePost);
    }

    public MatePostResponse updateMatePost(Long memberId, Long postId, MatePostUpdateRequest request, MultipartFile file) {
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

        // 새 파일이 있는 경우에만 유효성 검증 수행
        FileValidator.validateMatePostImage(newFile);

        // 기존 파일이 있으면 삭제
        if (currentImageUrl != null) {
            FileUploader.deleteFile(currentImageUrl);
        }

        // 새 파일 업로드
        return FileUploader.uploadFile(newFile);
    }

    public MatePostResponse updateMatePostStatus(Long memberId, Long postId, MatePostStatusRequest request) {
        MatePost matePost = findMatePostById(postId);

        validateAuthorization(matePost, memberId);
        validatePostStatus(matePost.getStatus());
        matePost.changeStatus(request.getStatus());

        return MatePostResponse.from(matePost);
    }

    public void deleteMatePost(Long memberId, Long postId) {
        MatePost matePost = findMatePostById(postId);

        validateAuthorization(matePost, memberId);
        validatePostStatus(matePost.getStatus());

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

        List<Member> participants = findAndValidateParticipants(request.getParticipantIds(), matePost.getMaxParticipants());

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

        return MateReviewCreateResponse.from(savedReview);
    }

    private void validateReviewEligibility(MatePost matePost, Member reviewer, Member reviewee) {
        // 리뷰어와 리뷰 대상자 모두 참여자(또는 방장) 여부 검증
        validateParticipant(matePost, reviewer);
        validateParticipant(matePost, reviewee);

        // 자기 자신 리뷰 검증
        validateSelfReview(reviewer, reviewee);
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

    private void validateSelfReview(Member reviewer, Member reviewee) {
        if (reviewer.equals(reviewee)) {
            throw new CustomException(SELF_REVIEW_NOT_ALLOWED);
        }
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
