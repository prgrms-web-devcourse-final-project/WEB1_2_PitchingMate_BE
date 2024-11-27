package com.example.mate.domain.goods.dto;

import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class MemberInfo {

    private Long memberId;
    private String nickname;
    private Float manner;
    private Role role;

    @Builder
    public MemberInfo(Long memberId, String nickname, Float manner, Role role) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.manner = manner;
        this.role = role;
    }

    public static MemberInfo from(Member member, Role role) {
        return MemberInfo.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .manner(member.getManner())
                .role(role)
                .build();
    }
}
