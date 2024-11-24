package com.example.mate.domain.goods.dto.request;

import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.vo.Location;
import lombok.Getter;

@Getter
public class GoodsPostRequest {

    private String teamName;
    private String title;
    private Category category;
    private Integer price;
    private String content;
    private Location location;
}
