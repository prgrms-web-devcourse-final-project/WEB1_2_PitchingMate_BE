package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MateChatRoomResponse {
    private Long roomId;
    private Long matePostId;
    private Long memberId;
    private Integer currentMembers;
    private PageResponse<MateChatMessageResponse> initialMessages;  // 추가

    public static MateChatRoomResponse from(
            MateChatRoom chatRoom,
            MateChatRoomMember member,
            PageResponse<MateChatMessageResponse> messages) {
        return MateChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .matePostId(chatRoom.getMatePost().getId())
                .memberId(member.getMember().getId())
                .currentMembers(chatRoom.getCurrentMembers())
                .initialMessages(messages)
                .build();
    }
}