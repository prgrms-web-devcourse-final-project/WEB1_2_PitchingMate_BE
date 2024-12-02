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
public class MatePostSummaryResponse {

    private String imageUrl;
    private String title;
    private Status status;
    private String myTeamName;
    private String rivalTeamName;
    private LocalDateTime matchTime;
    private String location;
    private Integer maxParticipants;
    private Age age;
    private Gender gender;
    private TransportType transportType;
    private Long postId;

    public static MatePostSummaryResponse from(MatePost post) {
        // 게시글 작성자의 팀이 myTeam
        String myTeamName = TeamInfo.getById(post.getTeamId()).shortName;
        String rivalTeamName = getRivalTeamName(post);

        return MatePostSummaryResponse.builder()
                .imageUrl(post.getImageUrl())
                .title(post.getTitle())
                .status(post.getStatus())
                .myTeamName(myTeamName)
                .rivalTeamName(rivalTeamName)
                .matchTime(post.getMatch().getMatchTime())
                .location(post.getMatch().getStadium().name)
                .maxParticipants(post.getMaxParticipants())
                .age(post.getAge())
                .gender(post.getGender())
                .transportType(post.getTransport())
                .postId(post.getId())
                .build();
    }

    private static String getRivalTeamName(MatePost post) {
        Match match = post.getMatch();
        Long postTeamId = post.getTeamId();

        if (postTeamId.equals(match.getHomeTeamId())) {
            return TeamInfo.getById(match.getAwayTeamId()).shortName;
        } else {
            return TeamInfo.getById(match.getHomeTeamId()).shortName;
        }
    }
}
