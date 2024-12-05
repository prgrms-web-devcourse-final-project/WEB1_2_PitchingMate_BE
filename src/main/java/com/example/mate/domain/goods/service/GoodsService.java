package com.example.mate.domain.goods.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.utils.file.FileUploader;
import com.example.mate.common.utils.file.FileValidator;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.GoodsReview;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostImageRepository;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.ArrayList;
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
public class GoodsService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsPostImageRepository imageRepository;
    private final GoodsReviewRepository reviewRepository;

    public GoodsPostResponse registerGoodsPost(Long memberId, GoodsPostRequest request, List<MultipartFile> files) {
        Member seller = findMemberById(memberId);
        validateTeamInfo(request.getTeamId());
        FileValidator.validateGoodsPostImages(files);

        GoodsPost goodsPost = GoodsPostRequest.toEntity(seller, request);
        GoodsPost savedPost = goodsPostRepository.save(goodsPost);

        attachImagesToGoodsPost(savedPost, files);

        return GoodsPostResponse.of(savedPost);
    }

    public GoodsPostResponse updateGoodsPost(Long memberId, Long goodsPostId, GoodsPostRequest request, List<MultipartFile> files) {
        Member seller = findMemberById(memberId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);

        validateTeamInfo(request.getTeamId());
        validateOwnership(seller, goodsPost);
        FileValidator.validateGoodsPostImages(files);

        GoodsPost updateTarget = GoodsPostRequest.toEntity(seller, request);
        goodsPost.updatePostDetails(updateTarget);

        deleteExistingImageFiles(goodsPostId);
        attachImagesToGoodsPost(goodsPost, files);

        return GoodsPostResponse.of(goodsPost);
    }

    public void deleteGoodsPost(Long memberId, Long goodsPostId) {
        Member seller = findMemberById(memberId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        validateGoodsPostDeletion(seller, goodsPost);

        deleteExistingImageFiles(goodsPostId);
        goodsPostRepository.delete(goodsPost);
    }

    @Transactional(readOnly = true)
    public GoodsPostResponse getGoodsPost(Long goodsPostId) {
        return GoodsPostResponse.of(findGoodsPostById(goodsPostId));
    }

    @Transactional(readOnly = true)
    public List<GoodsPostSummaryResponse> getMainGoodsPosts(Long teamId) {
        validateTeamInfo(teamId);

        return goodsPostRepository.findMainGoodsPosts(teamId, Status.OPEN, PageRequest.of(0, 4))
                .stream()
                .map(this::convertToSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsPostSummaryResponse> getPageGoodsPosts(Long teamId, String categoryVal, Pageable pageable) {
        validateTeamInfo(teamId);
        Category category = Category.from(categoryVal);

        Page<GoodsPost> pageGoodsPosts = goodsPostRepository.findPageGoodsPosts(teamId, Status.OPEN, category, pageable);
        List<GoodsPostSummaryResponse> responses = pageGoodsPosts.getContent().stream()
                .map(this::convertToSummaryResponse).toList();

        return PageResponse.from(pageGoodsPosts, responses);
    }

    public void completeTransaction(Long sellerId, Long goodsPostId, Long buyerId) {
        Member seller = findMemberById(sellerId);
        Member buyer = findMemberById(buyerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);

        validateTransactionEligibility(seller, buyer, goodsPost);
        goodsPost.completeTransaction(buyer);
    }

    public GoodsReviewResponse registerGoodsReview(Long reviewerId, Long goodsPostId, GoodsReviewRequest request) {
        Member reviewer = findMemberById(reviewerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        validateReviewEligibility(goodsPost, reviewer);

        GoodsReview review = request.toEntity(goodsPost, reviewer, goodsPost.getSeller());
        return GoodsReviewResponse.of(reviewRepository.save(review));
    }

    private void attachImagesToGoodsPost(GoodsPost goodsPost, List<MultipartFile> files) {
        List<GoodsPostImage> images = uploadImageFiles(files, goodsPost);
        goodsPost.changeImages(images);
    }

    private List<GoodsPostImage> uploadImageFiles(List<MultipartFile> files, GoodsPost savedPost) {
        List<GoodsPostImage> images = new ArrayList<>();

        for (MultipartFile file : files) {
            String uploadUrl = FileUploader.uploadFile(file);
            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl(uploadUrl)
                    .post(savedPost)
                    .build();
            images.add(image);
        }
        return images;
    }

    private void deleteExistingImageFiles(Long goodsPostId) {
        List<String> imageUrls = imageRepository.getImageUrlsByPostId(goodsPostId);
        imageUrls.forEach(url -> {
            if (!FileUploader.deleteFile(url)) {
                throw new CustomException(ErrorCode.FILE_DELETE_ERROR);
            }
        });
        imageRepository.deleteAllByPostId(goodsPostId);
    }

    private GoodsPostSummaryResponse convertToSummaryResponse(GoodsPost goodsPost) {
        String mainImageUrl = goodsPost.getMainImageUrl();
        return GoodsPostSummaryResponse.of(goodsPost, mainImageUrl);
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

    private void validateReviewEligibility(GoodsPost goodsPost, Member reviewer) {
        if (goodsPost.getStatus() != Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_REVIEW_STATUS_NOT_CLOSED);
        }

        if (!goodsPost.getBuyer().equals(reviewer)) {
            throw new CustomException(ErrorCode.GOODS_REVIEW_NOT_ALLOWED_FOR_NON_BUYER);
        }

        if (reviewRepository.existsByGoodsPostIdAndReviewerId(goodsPost.getId(), reviewer.getId())) {
            throw new CustomException(ErrorCode.GOODS_REVIEW_ALREADY_EXISTS);
        }
    }
}
