package com.example.mate.domain.goods.dto.response;

import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.members.entity.Team;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsPostSummaryResponse {

    private final Long id;
    private final Long teamId;
    private final String title;
    private final String category;
    private final Integer price;
    private final String imageUrl;

    /*
    팀 선택에 따른 굿즈 거래글 요약 조회 요청을 GoodsPostSummaryResponse로 반환
    거래글 id, 제목, 카테고리, 가격, 이미지 경로는 하드코딩
    요청한 team과 cateogory에 따른 반환 값 확인
     */
    public static GoodsPostSummaryResponse createResponse(Team team) {
        return GoodsPostSummaryResponse.builder()
                .id(1L)
                .teamId(team.getId())
                .title("NC 다이노스 배틀크러쉬 모자")
                .category(Category.CAP.getValue())
                .price(40000)
                .imageUrl("upload/thumbnail.png")
                .build();
    }

    public static GoodsPostSummaryResponse createResponse(Team team, Category category) {
        return GoodsPostSummaryResponse.builder()
                .id(1L)
                .teamId(team.getId())
                .title("NC 다이노스 배틀크러쉬 모자")
                .category(category.getValue())
                .price(40000)
                .imageUrl("upload/thumbnail.png")
                .build();
    }

}
