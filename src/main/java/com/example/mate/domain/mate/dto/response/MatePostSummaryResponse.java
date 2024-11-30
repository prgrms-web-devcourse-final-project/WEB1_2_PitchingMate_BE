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
    private LocalDateTime rivalMatchTime;
    private String location;
    private Integer maxParticipants;
    private Age age;
    private Gender gender;
    private TransportType transportType;

    public static MatePostSummaryResponse from(MatePost post, Long selectedTeamId) {
        Match match = post.getMatch();
        String myTeamName;
        String rivalTeamName;

        if (selectedTeamId != null) {
            // 특정 팀 선택한 경우: 게시글 작성자의 팀이 myTeam
            myTeamName = TeamInfo.getById(post.getTeamId()).shortName;
            rivalTeamName = getRivalTeamName(post);
        } else {
            // KBO 선택한 경우: 홈팀이 myTeam
            myTeamName = TeamInfo.getById(match.getHomeTeamId()).shortName;
            rivalTeamName = TeamInfo.getById(match.getAwayTeamId()).shortName;
        }

        return MatePostSummaryResponse.builder()
                .imageUrl(post.getImageUrl())
                .title(post.getTitle())
                .status(post.getStatus())
                .myTeamName(myTeamName)
                .rivalTeamName(rivalTeamName)
                .rivalMatchTime(match.getMatchTime())
                .location(match.getStadium().name)
                .maxParticipants(post.getMaxParticipants())
                .age(post.getAge())
                .gender(post.getGender())
                .transportType(post.getTransport())
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
