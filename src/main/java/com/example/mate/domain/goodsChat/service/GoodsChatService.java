package com.example.mate.domain.goodsChat.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsChatService {

    private final GoodsPostRepository goodsPostRepository;
    private final MemberRepository memberRepository;
    private final GoodsChatRoomRepository chatRoomRepository;

    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(Long buyerId, Long goodsPostId) {
        Member buyer = findMemberById(buyerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        Member seller = goodsPost.getSeller();

        validateCreateChatRoom(goodsPost, buyer, seller);

        // 구매자가 채팅방이 존재하면 기존 채팅방을 반환하고, 없다면 새로 생성하여 반환
        GoodsChatRoom goodsChatRoom = chatRoomRepository.findExistingChatRoom(goodsPostId, buyerId, Role.BUYER)
                .orElseGet(() -> createChatRoom(goodsPost, buyer, seller));

        return GoodsChatRoomResponse.of(goodsChatRoom);
    }

    private void validateCreateChatRoom(GoodsPost goodsPost, Member seller, Member buyer) {
        if (goodsPost.getStatus() == Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_CHAT_CLOSED_POST);
        }
        if (seller == buyer) {
            throw new CustomException(ErrorCode.GOODS_CHAT_SELLER_CANNOT_START);
        }
    }

    private GoodsChatRoom createChatRoom(GoodsPost goodsPost, Member buyer, Member seller) {
        GoodsChatRoom goodsChatRoom = GoodsChatRoom.builder()
                .goodsPost(goodsPost)
                .build();
        
        GoodsChatRoom savedChatRoom = chatRoomRepository.save(goodsChatRoom);

        savedChatRoom.addChatParticipant(buyer, Role.BUYER);
        savedChatRoom.addChatParticipant(seller, Role.SELLER);

        return savedChatRoom;
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private GoodsPost findGoodsPostById(Long goodsPostId) {
        return goodsPostRepository.findById(goodsPostId).orElseThrow(() ->
                new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));
    }
}
