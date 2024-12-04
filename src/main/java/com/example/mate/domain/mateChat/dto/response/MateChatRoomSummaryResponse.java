package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MateChatRoomSummaryResponse {
    private Long roomId;
    private Integer currentMembers;
    private Integer maxMembers;
    private Boolean isActive;

    public static MateChatRoomSummaryResponse from(MateChatRoom chatRoom) {
        return MateChatRoomSummaryResponse.builder()
                .roomId(chatRoom.getId())
                .currentMembers(chatRoom.getCurrentMembers())
                .maxMembers(10)
                .isActive(chatRoom.getActive())
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
