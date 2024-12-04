package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MateChatRoomSummaryResponse {
    private Long roomId;
    private Long postId;
    private String postImageUrl;
    private String postTitle;
    private Integer currentMembers;
    private Boolean isActive;
    private Integer maxMembers;


    public static MateChatRoomSummaryResponse from(MateChatRoom chatRoom) {
        return MateChatRoomSummaryResponse.builder()
                .roomId(chatRoom.getId())
                .postId(chatRoom.getMatePost().getId())
                .postImageUrl(chatRoom.getMatePost().getImageUrl())
                .postTitle(chatRoom.getMatePost().getTitle())
                .currentMembers(chatRoom.getCurrentMembers())
                .isActive(chatRoom.getActive())
                .maxMembers(10)
                .build();
    }

    public static MateChatRoomSummaryResponse empty() {
        return MateChatRoomSummaryResponse.builder()
                .currentMembers(0)
                .maxMembers(10)
                .isActive(false)
                .build();
    }
}
