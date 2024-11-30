package com.example.mate.domain.mate.dto.response;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatePostDetailResponse {
    private String postImageUrl;
    private String title;
    private Status status;
    private String rivalTeamName;
    private LocalDateTime rivalMatchTime;
    private String location;
    private Age age;
    private Gender gender;
    private TransportType transportType;
    private Integer maxParticipants;
    private String userImageUrl;
    private String nickname;
    private Float manner;
    private String content;
    private Long postId;

    public static MatePostDetailResponse from(MatePost post) {
        return MatePostDetailResponse.builder()
                .postImageUrl(post.getImageUrl())
                .title(post.getTitle())
                .status(post.getStatus())
                .rivalTeamName(getRivalTeamName(post))
                .rivalMatchTime(post.getMatch().getMatchTime())
                .location(post.getMatch().getStadium().name)
                .age(post.getAge())
                .gender(post.getGender())
                .transportType(post.getTransport())
                .maxParticipants(post.getMaxParticipants())
                .userImageUrl(post.getAuthor().getImageUrl())
                .nickname(post.getAuthor().getNickname())
                .manner(post.getAuthor().getManner())
                .content(post.getContent())
                .postId(post.getId())
                .build();
    }

    private static String getRivalTeamName(MatePost post) {
        Match match = post.getMatch();
        Long postTeamId = post.getTeamId(); // 게시글 작성자가 선택한 팀

        // 게시글 작성자가 선택한 팀이 홈팀인 경우 원정팀이 상대팀
        if (postTeamId.equals(match.getHomeTeamId())) {
            return TeamInfo.getById(match.getAwayTeamId()).shortName;
        }
        // 게시글 작성자가 선택한 팀이 원정팀인 경우 홈팀이 상대팀
        else {
            return TeamInfo.getById(match.getHomeTeamId()).shortName;
        }
    }
}
