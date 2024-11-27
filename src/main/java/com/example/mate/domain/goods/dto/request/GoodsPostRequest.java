package com.example.mate.domain.goods.dto.request;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.constant.TeamInfo.Team;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoodsPostRequest {

    private Long teamId;
    private String title;
    private Category category;
    private Integer price;
    private String content;
    private LocationInfo location;

    public static GoodsPost toEntity(Member seller, GoodsPostRequest request) {
        Team team = TeamInfo.getById(request.getTeamId());
        LocationInfo locationInfo = request.getLocation();

        return GoodsPost.builder()
                .seller(seller)
                .teamId(team.id)
                .title(request.getTitle())
                .content(request.getContent())
                .price(request.getPrice())
                .category(request.getCategory())
                .location(LocationInfo.toEntity(locationInfo))
                .build();
    }
}
