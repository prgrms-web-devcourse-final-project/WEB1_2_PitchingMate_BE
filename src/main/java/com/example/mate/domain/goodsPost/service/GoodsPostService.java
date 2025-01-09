package com.example.mate.domain.goodsPost.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileService;
import com.example.mate.domain.file.FileValidator;
import com.example.mate.domain.goodsPost.dto.request.GoodsPostRequest;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostResponse;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostImageRepository;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.ActivityType;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
public class GoodsPostService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsPostImageRepository imageRepository;
    private final FileService fileService;

    public GoodsPostResponse registerGoodsPost(Long memberId, GoodsPostRequest request, List<MultipartFile> files) {
        Member seller = findMemberById(memberId);
        validateTeamInfo(request.getTeamId());
        FileValidator.validateGoodsPostImages(files);

        GoodsPost goodsPost = GoodsPostRequest.toEntity(seller, request);
        GoodsPost savedPost = goodsPostRepository.save(goodsPost);

        attachImagesToGoodsPost(savedPost, files);

        seller.updateManner(ActivityType.POST);
        memberRepository.save(seller);

        return GoodsPostResponse.of(savedPost);
    }

    public GoodsPostResponse updateGoodsPost(Long memberId, Long goodsPostId, GoodsPostRequest request, List<MultipartFile> files) {
        Member seller = findMemberById(memberId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);

        validateTeamInfo(request.getTeamId());
        validateOwnership(seller, goodsPost);

        GoodsPost updateTarget = GoodsPostRequest.toEntity(seller, request);
        goodsPost.updatePostDetails(updateTarget);
        updateGoodsPostImages(goodsPostId, goodsPost, files);

        return GoodsPostResponse.of(goodsPost);
    }

    private void updateGoodsPostImages(Long goodsPostId, GoodsPost goodsPost, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            FileValidator.validateGoodsPostImages(files);
            deleteExistingImageFiles(goodsPostId);
            attachImagesToGoodsPost(goodsPost, files);
        }
    }

    public void deleteGoodsPost(Long memberId, Long goodsPostId) {
        Member seller = findMemberById(memberId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        validateGoodsPostDeletion(seller, goodsPost);

        deleteExistingImageFiles(goodsPostId);
        goodsPostRepository.delete(goodsPost);

        seller.updateManner(ActivityType.DELETE);
        memberRepository.save(seller);
    }

    @Transactional(readOnly = true)
    public GoodsPostResponse getGoodsPost(Long goodsPostId) {
        return GoodsPostResponse.of(findGoodsPostById(goodsPostId));
    }

    @Transactional(readOnly = true)
    public List<GoodsPostSummaryResponse> getMainGoodsPosts(Long teamId) {
        validateTeamInfo(teamId);
        return mapToGoodsPostSummaryResponses(goodsPostRepository.findMainGoodsPosts(teamId, Status.OPEN, PageRequest.of(0, 4)));
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsPostSummaryResponse> getPageGoodsPosts(Long teamId, String categoryVal,
                                                                    Pageable pageable) {
        validateTeamInfo(teamId);
        Category category = Category.from(categoryVal);
        Page<GoodsPost> pageGoodsPosts = goodsPostRepository.findPageGoodsPosts(teamId, Status.OPEN, category,
                pageable);
        return PageResponse.from(pageGoodsPosts, mapToGoodsPostSummaryResponses(pageGoodsPosts.getContent()));
    }

    public void completeTransaction(Long sellerId, Long goodsPostId, Long buyerId) {
        Member seller = findMemberById(sellerId);
        Member buyer = findMemberById(buyerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);

        validateTransactionEligibility(seller, buyer, goodsPost);
        goodsPost.completeTransaction(buyer);

        seller.updateManner(ActivityType.GOODS);
        buyer.updateManner(ActivityType.GOODS);
    }

    private void attachImagesToGoodsPost(GoodsPost goodsPost, List<MultipartFile> files) {
        List<GoodsPostImage> images = uploadImageFiles(files, goodsPost);
        goodsPost.changeImages(images);
    }

    private List<GoodsPostImage> uploadImageFiles(List<MultipartFile> files, GoodsPost savedPost) {
        return IntStream.range(0, files.size())
                .mapToObj(i -> GoodsPostImage.builder()
                        // 첫번째 사진일 경우 썸네일과 함께 저장
                        .imageUrl(i == 0 ? fileService.uploadImageWithThumbnail(files.get(i)) : fileService.uploadFile(files.get(i)))
                        .post(savedPost)
                        .build())
                .collect(Collectors.toList());
    }

    private void deleteExistingImageFiles(Long goodsPostId) {
        List<String> imageUrls = imageRepository.getImageUrlsByPostId(goodsPostId);
        imageUrls.forEach(fileService::deleteFile);
        imageRepository.deleteAllByPostId(goodsPostId);
    }

    private List<GoodsPostSummaryResponse> mapToGoodsPostSummaryResponses(List<GoodsPost> goodsPosts) {
        return goodsPosts.stream()
                .map(goodsPost -> GoodsPostSummaryResponse.of(goodsPost, goodsPost.getMainImageUrl()))
                .toList();
    }

    private GoodsPost findGoodsPostById(Long goodsPostId) {
        return goodsPostRepository.findById(goodsPostId).orElseThrow(() ->
                new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private void validateTeamInfo(Long teamId) {
        if (teamId != null && !TeamInfo.existById(teamId)) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    private void validateGoodsPostDeletion(Member seller, GoodsPost goodsPost) {
        validateOwnership(seller, goodsPost);

        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_DELETE_NOT_ALLOWED);
        }
    }

    private void validateOwnership(Member seller, GoodsPost goodsPost) {
        if (goodsPost.getSeller() != seller) {
            throw new CustomException(ErrorCode.GOODS_MODIFICATION_NOT_ALLOWED);
        }
    }

    private void validateTransactionEligibility(Member seller, Member buyer, GoodsPost goodsPost) {
        if (!goodsPost.getSeller().equals(seller)) {
            throw new CustomException(ErrorCode.GOODS_MODIFICATION_NOT_ALLOWED);
        }
        if (seller.equals(buyer)) {
            throw new CustomException(ErrorCode.SELLER_CANNOT_BE_BUYER);
        }
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_ALREADY_COMPLETED);
        }
    }
}
