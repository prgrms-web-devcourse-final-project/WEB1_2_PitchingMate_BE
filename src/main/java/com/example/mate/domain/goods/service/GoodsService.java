package com.example.mate.domain.goods.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.utils.file.FileUploader;
import com.example.mate.common.utils.file.FileValidator;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostImageRepository;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GoodsService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsPostImageRepository imageRepository;

    public GoodsPostResponse registerGoodsPost(Long memberId, GoodsPostRequest request, List<MultipartFile> files) {
        // 사용자, 팀 정보, 이미지 파일 유효성 검증
        Member seller = getMemberOrThrow(memberId);
        validateTeamInfo(request.getTeamId());
        FileValidator.validateGoodsPostImages(files);

        // GoodsPost 엔티티 생성 & 저장
        GoodsPost goodsPost = GoodsPostRequest.toEntity(seller, request);
        GoodsPost savedPost = goodsPostRepository.save(goodsPost);

        // 이미지 저장 & 연관관계 설정
        List<GoodsPostImage> images = uploadImageFiles(files, savedPost);
        goodsPost.changeImages(images);

        return GoodsPostResponse.of(goodsPost);
    }

    public GoodsPostResponse updateGoodsPost(Long memberId, Long goodsPostId, GoodsPostRequest request, List<MultipartFile> files) {
        // 사용자, 판매글 정보, 팀 정보, 이미지 파일 유효성 검증
        Member seller = getMemberOrThrow(memberId);
        GoodsPost goodsPost = getGoodsPostOrThrow(seller, goodsPostId);

        validateTeamInfo(request.getTeamId());
        FileValidator.validateGoodsPostImages(files);
        deleteExistingImageFiles(goodsPostId);

        // 판매글 정보 업데이트
        GoodsPost updateTarget = GoodsPostRequest.toEntity(seller, request);
        List<GoodsPostImage> images = uploadImageFiles(files, goodsPost);
        goodsPost.updatePostDetails(updateTarget, images);

        return GoodsPostResponse.of(goodsPost);
    }

    public void deleteGoodsPost(Long memberId, Long goodsPostId) {
        // 사용자, 판매글 정보 유효성 검증
        Member seller = getMemberOrThrow(memberId);
        GoodsPost goodsPost = getGoodsPostOrThrow(seller, goodsPostId);

        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_DELETE_NOT_ALLOWED);
        }

        // 업로된 이미지 파일 삭제
        deleteExistingImageFiles(goodsPostId);
        goodsPostRepository.delete(goodsPost);
    }

    @Transactional(readOnly = true)
    public GoodsPostResponse getGoodsPost(Long goodsPostId) {
        GoodsPost goodsPost = goodsPostRepository.findById(goodsPostId).orElseThrow(()
                -> new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));

        return GoodsPostResponse.of(goodsPost);
    }

    @Transactional(readOnly = true)
    public List<GoodsPostSummaryResponse> getMainGoodsPosts(Long teamId) {
        validateTeamInfo(teamId);
        List<GoodsPost> goodsPosts = goodsPostRepository.findMainGoodsPosts(teamId, Status.OPEN, PageRequest.of(0, 4));

        return goodsPosts.stream()
                .map(this::convertToSummaryResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsPostSummaryResponse> getPageGoodsPosts(Long teamId, String categoryVal, Pageable pageable) {
        validateTeamInfo(teamId);
        Category category = Category.from(categoryVal);

        Page<GoodsPost> pageGoodsPosts = goodsPostRepository.findPageGoodsPosts(teamId, Status.OPEN, category, pageable);
        List<GoodsPostSummaryResponse> responses = pageGoodsPosts.getContent().stream()
                .map(this::convertToSummaryResponse).toList();

        return PageResponse.<GoodsPostSummaryResponse>builder()
                .content(responses)
                .totalPages(pageGoodsPosts.getTotalPages())
                .totalElements(pageGoodsPosts.getTotalElements())
                .hasNext(pageGoodsPosts.hasNext())
                .pageNumber(pageGoodsPosts.getNumber())
                .pageSize(pageGoodsPosts.getSize())
                .build();
    }

    public void completeTransaction(Long sellerId, Long goodsPostId, Long buyerId) {
        Member seller = getMemberOrThrow(sellerId);
        Member buyer = getMemberOrThrow(buyerId);
        GoodsPost goodsPost = getGoodsPostOrThrow(seller, goodsPostId);

        if (seller == buyer) {
            throw new CustomException(ErrorCode.SELLER_CANNOT_BE_BUYER);
        }
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_ALREADY_COMPLETED);
        }

        goodsPost.completeTransaction(buyer);
    }

    public GoodsReviewResponse registerGoodsReview(Long reviewerId, Long goodsPostId, GoodsReviewRequest request) {
        Member reviewer = findMemberById(reviewerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        validateReviewEligibility(goodsPost, reviewer);

        GoodsReview review = request.toEntity(goodsPost, reviewer, goodsPost.getSeller());
        return GoodsReviewResponse.of(reviewRepository.save(review));
    }

    private GoodsPost getGoodsPostOrThrow(Member seller, Long goodsPostId) {
        GoodsPost goodsPost = goodsPostRepository.findById(goodsPostId).orElseThrow(()
                -> new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));

        if (goodsPost.getSeller() != seller) {
            throw new CustomException(ErrorCode.GOODS_UPDATE_NOT_ALLOWED);
        }
        return goodsPost;
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

    private GoodsPostSummaryResponse convertToSummaryResponse(GoodsPost goodsPost) {
        String mainImageUrl = getMainImageUrl(goodsPost);
        return GoodsPostSummaryResponse.of(goodsPost, mainImageUrl);
    }

    private String getMainImageUrl(GoodsPost goodsPost) {
        return goodsPost.getGoodsPostImages().stream()
                .filter(GoodsPostImage::getIsMainImage)
                .findFirst()
                .map(GoodsPostImage::getImageUrl)
                .orElse("upload/default.jpg");
    }
}
