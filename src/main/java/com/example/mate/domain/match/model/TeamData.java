package com.example.mate.domain.match.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamData {
    private final Long id;
    private final String name;
    private final String stadiumName;
    private final String location;
    private final String longitude;
    private final String latitude;

    public static final List<TeamData> TEAM_LIST = Arrays.asList(
            new TeamData(1L, "KIA 타이거즈", "광주-기아 챔피언스 필드", "광주광역시 북구", "126.8989", "35.1681"),
            new TeamData(2L, "LG 트윈스", "잠실야구장", "서울특별시 송파구", "127.0719", "37.5122"),
            new TeamData(3L, "NC 다이노스", "창원NC파크", "창원시 마산회원구", "128.5829", "35.2534"),
            new TeamData(4L, "SSG 랜더스", "인천SSG랜더스필드", "인천광역시 미추홀구", "126.6781", "37.4374"),
            new TeamData(5L, "kt wiz", "수원 kt wiz 파크", "수원시 장안구", "127.0355", "37.2994"),
            new TeamData(6L, "두산 베어스", "잠실야구장", "서울특별시 송파구", "127.0719", "37.5122"),
            new TeamData(7L, "롯데 자이언츠", "사직야구장", "부산광역시 동래구", "129.0639", "35.1947"),
            new TeamData(8L, "삼성 라이온즈", "대구삼성라이온즈파크", "대구광역시 수성구", "128.6814", "35.8409"),
            new TeamData(9L, "키움 히어로즈", "고척스카이돔", "서울특별시 구로구", "126.8669", "37.4982"),
            new TeamData(10L, "한화 이글스", "한화생명이글스파크", "대전광역시 중구", "127.4294", "36.3172")
    );
}
