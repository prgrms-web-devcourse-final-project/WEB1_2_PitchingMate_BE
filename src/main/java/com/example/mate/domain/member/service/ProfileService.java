package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;

    // 굿즈 판매기록 페이징 조회
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
}
