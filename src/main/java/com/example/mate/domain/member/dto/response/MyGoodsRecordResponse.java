package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.goods.entity.GoodsPost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyGoodsRecordResponse {

    @Schema(description = "굿즈 게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "굿즈 게시글 제목", example = "야구 장갑 팝니다.")
    private String title;

    @Schema(description = "굿즈 게시글 대표 사진 url", example = "default.png")
    private String imageUrl;

    @Schema(description = "굿즈 가격", example = "10000")
    private Integer price;

    @Schema(description = "굿즈 게시글 작성자 닉네임", example = "이대호")
    private String author;
    
    @Schema(description = "굿즈 게시글 작성 시간", example = "2024-12-01T20:39:41.746367")
    private LocalDateTime createdAt;

    public static MyGoodsRecordResponse from() {
        return MyGoodsRecordResponse.builder()
                .postId(3L)
                .title("이대호 레전드 피규어 팝니다!")
                .imageUrl("/images/legend-lee.png")
                .price(50000)
                .author("이대호가 좋아서")
                .createdAt(LocalDateTime.now().minusDays(7))
                .build();
    }

    public static MyGoodsRecordResponse of(GoodsPost goodsPost, String mainImageUrl) {
        return MyGoodsRecordResponse.builder()
                .postId(goodsPost.getId())
                .title(goodsPost.getTitle())
                .imageUrl(mainImageUrl)
                .price(goodsPost.getPrice())
                .author(goodsPost.getSeller().getNickname())
                .createdAt(goodsPost.getCreatedAt())
                .build();
    }
}
