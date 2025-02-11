package com.example.mate.domain.goodsPost.dto.request;

import com.example.mate.common.util.validator.ValidEnum;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.member.entity.Member;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoodsPostRequest {

    @Min(value = 0, message = "팀 ID는 0 이상이어야 합니다.")
    @Max(value = 10, message = "팀 ID는 10 이하이어야 합니다.")
    private Long teamId;

    @NotEmpty(message = "제목은 필수 입력 값입니다.")
    @Size(min = 1, max = 20, message = "제목은 20자 이하로 입력해야 합니다.")
    private String title;

    @ValidEnum(message = "카테고리의 입력 값이 잘못되었습니다.", enumClass = Category.class)
    private Category category;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Max(value = 2_100_000_000, message = "가격은 21억 이하로 입력해야 합니다.")
    private Integer price;

    @NotEmpty(message = "내용은 필수 입력 값입니다.")
    @Size(min = 1, max = 500, message = "내용은 1자 이상 500 이하로 입력해야 합니다.")
    private String content;

    @NotNull(message = "위치 정보는 필수 입력 값입니다.")
    private LocationInfo location;

    public static GoodsPost toEntity(Member seller, GoodsPostRequest request) {
        LocationInfo locationInfo = request.getLocation();

        return GoodsPost.builder()
                .seller(seller)
                .teamId(request.getTeamId())
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .category(request.getCategory())
                .location(LocationInfo.toEntity(locationInfo))
                .build();
    }
}
