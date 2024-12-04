package com.example.mate.domain.goodsChat.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsChatPartId implements Serializable {

    private Long member;
    private Long goodsChatRoom;
}
