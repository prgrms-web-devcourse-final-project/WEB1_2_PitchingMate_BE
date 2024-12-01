package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.MateReviewRepositoryCustom;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final MateReviewRepository mateReviewRepository;
    private final MateReviewRepositoryCustom mateReviewRepositoryCustom;

    // 굿즈 판매기록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyGoodsRecordResponse> getSoldGoodsPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<GoodsPost> soldGoodsPage = goodsPostRepository.findGoodsPostsBySellerId(memberId, Status.CLOSED,
                pageable);

        List<MyGoodsRecordResponse> content = soldGoodsPage.getContent().stream()
                .map(this::convertToRecordResponse).toList();

        return PageResponse.<MyGoodsRecordResponse>builder()
                .content(content)
                .totalPages(soldGoodsPage.getTotalPages())
                .totalElements(soldGoodsPage.getTotalElements())
                .hasNext(soldGoodsPage.hasNext())
                .pageNumber(soldGoodsPage.getNumber())
                .pageSize(soldGoodsPage.getSize())
                .build();
    }

    // 굿즈 구매기록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyGoodsRecordResponse> getBoughtGoodsPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<GoodsPost> boughtGoodsPage = goodsPostRepository.findGoodsPostsByBuyerId(memberId, Status.CLOSED,
                pageable);

        List<MyGoodsRecordResponse> content = boughtGoodsPage.getContent().stream()
                .map(this::convertToRecordResponse).toList();

        return PageResponse.<MyGoodsRecordResponse>builder()
                .content(content)
                .totalPages(boughtGoodsPage.getTotalPages())
                .totalElements(boughtGoodsPage.getTotalElements())
                .hasNext(boughtGoodsPage.hasNext())
                .pageNumber(boughtGoodsPage.getNumber())
                .pageSize(boughtGoodsPage.getSize())
                .build();
    }

    private void validateMemberId(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private MyGoodsRecordResponse convertToRecordResponse(GoodsPost goodsPost) {
        String mainImageUrl = getMainImageUrl(goodsPost);
        return MyGoodsRecordResponse.of(goodsPost, mainImageUrl);
    }

    private String getMainImageUrl(GoodsPost goodsPost) {
        return goodsPost.getGoodsPostImages().stream()
                .filter(GoodsPostImage::getIsMainImage)
                .findFirst()
                .map(GoodsPostImage::getImageUrl)
                .orElse("upload/default.jpg");
    }

    // 메이트 후기 페이징 조회
    public PageResponse<MyReviewResponse> getMateReviewPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<MyReviewResponse> mateReviewPage = mateReviewRepositoryCustom.findMateReviewsByRevieweeId(
                memberId, pageable);

        return PageResponse.<MyReviewResponse>builder()
                .content(mateReviewPage.getContent())
                .totalPages(mateReviewPage.getTotalPages())
                .totalElements(mateReviewPage.getTotalElements())
                .hasNext(mateReviewPage.hasNext())
                .pageNumber(mateReviewPage.getNumber())
                .pageSize(mateReviewPage.getSize())
                .build();
    }
}