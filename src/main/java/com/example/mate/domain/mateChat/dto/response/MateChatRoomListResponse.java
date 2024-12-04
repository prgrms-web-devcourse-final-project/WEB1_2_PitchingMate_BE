package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatRoomListResponse {
    private Long roomId;
    private Long postId;
    private String postImageUrl;
    private String postTitle;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private Integer currentMembers;
    private Boolean isActive;

    public static MateChatRoomListResponse from(MateChatRoom chatRoom) {
        return MateChatRoomListResponse.builder()
                .roomId(chatRoom.getId())
                .postId(chatRoom.getMatePost().getId())
                .postImageUrl(chatRoom.getMatePost().getImageUrl())
                .postTitle(chatRoom.getMatePost().getTitle())
                .lastMessageContent(chatRoom.getLastChatContent())
                .lastMessageTime(chatRoom.getLastChatSentAt())
                .currentMembers(chatRoom.getCurrentMembers())
                .isActive(chatRoom.getActive())
                .build();
    }
}
