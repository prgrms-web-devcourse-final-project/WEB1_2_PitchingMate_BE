package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatRoomResponse {
    private Long roomId;
    private Long matePostId;
    private Integer currentMembers;
    private LocalDateTime joinedAt;

    public static MateChatRoomResponse from(MateChatRoom chatRoom, MateChatRoomMember member) {
        return MateChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .matePostId(chatRoom.getMatePost().getId())
                .currentMembers(chatRoom.getCurrentMembers())
                .joinedAt(member.getCreatedAt())
                .build();
    }
}