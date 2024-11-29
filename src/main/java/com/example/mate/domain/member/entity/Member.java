package com.example.mate.domain.member.entity;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.member.dto.request.JoinRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", length = 40, nullable = false, unique = true)
    private String email;

    @Builder.Default
    @Column(name = "image_url", nullable = false)
    private String imageUrl = "/images/default.png"; // TODO : 이미지 기본 경로 설정 필요

    @Column(name = "age", nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "team_id")
    private Long teamId;

    @Builder.Default
    @Column(name = "manner", nullable = false)
    private Float manner = 0.300F;

    @Column(name = "about_me", length = 100)
    private String aboutMe;

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void changeTeam(TeamInfo.Team team) {
        this.teamId = team != null ? team.id : null;
    }

    public void changeAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public static Member from(JoinRequest request) {
        return Member.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .age(LocalDate.now().getYear() - Integer.parseInt(request.getBirthyear()))
                .gender(Gender.fromCode(request.getGender()))
                .teamId(request.getTeamId())
                .build();
    }
}
