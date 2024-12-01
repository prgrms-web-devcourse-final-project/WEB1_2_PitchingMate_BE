package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.goods.entity.GoodsPost;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyGoodsRecordResponse {
    private Long postId;
    private String title;
    private String imageUrl;
    private Integer price;
    private String author;
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