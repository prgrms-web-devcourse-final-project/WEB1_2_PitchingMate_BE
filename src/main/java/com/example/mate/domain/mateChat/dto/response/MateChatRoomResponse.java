package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MateChatRoomResponse {
    private Long roomId;
    private Long matePostId;
    private Long memberId;
    private Integer currentMembers;
    private Boolean isRoomActive;
    private Boolean isMessageable;
    private Boolean isAuthorLeft;
    private Boolean isAuthor;
    private List<MateChatMessageResponse> initialMessages;

    public static MateChatRoomResponse from(
            MateChatRoom chatRoom,
            MateChatRoomMember member,
            List<MateChatMessageResponse> messages) {
        return MateChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .matePostId(chatRoom.getMatePost().getId())
                .memberId(member.getMember().getId())
                .currentMembers(chatRoom.getCurrentMembers())
                .isRoomActive(chatRoom.getIsActive())
                .isMessageable(chatRoom.getIsMessageable())
                .isAuthorLeft(chatRoom.getIsAuthorLeft())
                .isAuthor(chatRoom.getMatePost().getAuthor().getId().equals(member.getMember().getId()))
                .initialMessages(messages)
                .build();
    }
}