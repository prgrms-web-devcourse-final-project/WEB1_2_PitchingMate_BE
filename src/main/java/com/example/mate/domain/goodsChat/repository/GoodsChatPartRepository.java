package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsChatPartRepository extends JpaRepository<GoodsChatPart, GoodsChatPartId> {

}