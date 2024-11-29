package com.example.mate.domain.mate.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.request.MatePostSearchRequest;
import com.example.mate.domain.mate.dto.request.MatePostStatusRequest;
import com.example.mate.domain.mate.dto.response.MatePostDetailResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.mate.common.error.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MateService {

    private final MateRepository mateRepository;
    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;

    public MatePostResponse createMatePost(MatePostCreateRequest request, MultipartFile file) {
        Member author = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND_BY_ID));


        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new CustomException(MATCH_NOT_FOUND_BY_ID));

        if (!TeamInfo.existById(request.getTeamId())) {
            throw new CustomException(TEAM_NOT_FOUND);
        }

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

    public List<MatePostSummaryResponse> getMainPagePosts(Long teamId) {
        if (teamId != null && !TeamInfo.existById(teamId)) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
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
                .collect(Collectors.toList());
    }

    public PageResponse<MatePostSummaryResponse> getMatePagePosts(MatePostSearchRequest request, Pageable pageable) {
        if (request.getTeamId()!= null && !TeamInfo.existById(request.getTeamId())) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
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

    public MatePostDetailResponse getMatePostDetail(Long postId) {
        MatePost matePost = mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(MATE_POST_NOT_FOUND_BY_ID));

        return MatePostDetailResponse.from(matePost);
    }

    public MatePostResponse updateMatePostStatus(Long memberId, Long postId, MatePostStatusRequest request) {
        MatePost matePost = mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(MATE_POST_NOT_FOUND_BY_ID));

        matePost.validateAuthor(memberId);
        matePost.changeStatus(request.getStatus());

        return MatePostResponse.from(matePost);
    }

    public void deleteMatePost(Long memberId, Long postId) {
        MatePost matePost = mateRepository.findById(postId)
                .orElseThrow(() -> new CustomException(MATE_POST_NOT_FOUND_BY_ID));

        matePost.validateAuthor(memberId);

        if (matePost.getStatus() == Status.COMPLETE) {
            matePost.getVisit().detachPost();
        }

        mateRepository.delete(matePost);
    }
}
