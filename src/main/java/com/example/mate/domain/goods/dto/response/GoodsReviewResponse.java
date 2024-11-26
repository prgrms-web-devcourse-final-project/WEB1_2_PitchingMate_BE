package com.example.mate.domain.goods.dto.response;

import com.example.mate.domain.goods.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goods.entity.Rating;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsReviewResponse {

    private final Long id;
    private final Rating rating;
    private final String reviewContent;
    private final String reviewer;
    private final Long revieweeId;
    private final LocalDateTime createdAt;
    private final Long goodsPostId;
    private final String goodsPostTitle;

    /*
    굿즈 거래후기 등록/조회 요청을 GoodsReviewResponse로 반환
    거래후기 id, 리뷰어 닉네임, 굿즈 거래글 제목은 하드코딩
    goodsPostId와 request 값에 따른 반환 값 확인
     */
    public static GoodsReviewResponse createResponse(Long goodsPostId, GoodsReviewRequest request) {
        return GoodsReviewResponse.builder()
                .id(1L)
                .rating(request.getRating())
                .reviewContent(request.getReviewContent())
                .reviewer("볼빨간사촌형") // request에서 reviewerId를 전달받아, 서비스에서 해당 id의 회원 닉네임 반환을 가정. 후기 보내기 이후 화면 구성에 따라 수정 필요
                .revieweeId(1L)
                .createdAt(LocalDateTime.now())
                .goodsPostId(goodsPostId)
                .goodsPostTitle("쁘띠 이대호 피규어 & 쁘띠 이대호 빠따")
                .build();
    }

    public static GoodsReviewResponse createResponse() {
        return GoodsReviewResponse.builder()
                .id(1L)
                .rating(Rating.GOOD)
                .reviewContent("물건이 너무 좋아요. 친절해요.")
                .reviewer("볼빨간사촌형")
                .revieweeId(1L)
                .createdAt(LocalDateTime.now())
                .goodsPostId(1L)
                .goodsPostTitle("쁘띠 이대호 피규어 & 쁘띠 이대호 빠따")
                .build();
    }
}
