package com.example.mate.domain.constant;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;

import java.util.List;

public final class StadiumInfo {

    public static final class Stadium {
        public final Long id;
        public final String name;
        public final String location;
        public final String longitude;
        public final String latitude;

        private Stadium(Long id, String name, String location, String longitude, String latitude) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }

    public static final Stadium GWANGJU = new Stadium(1L, "광주-기아 챔피언스 필드", "광주광역시 북구", "126.8891056", "35.1681242");
    public static final Stadium JAMSIL = new Stadium(2L, "잠실야구장", "서울특별시 송파구", "127.071827", "37.5120673");
    public static final Stadium CHANGWON = new Stadium(3L, "창원 NC 파크", "창원시 마산회원구", "128.5822292", "35.2225967");
    public static final Stadium INCHEON = new Stadium(4L, "인천 SSG 랜더스필드", "인천광역시 미추홀구", "126.6932617", "37.4370423");
    public static final Stadium SUWON = new Stadium(5L, "수원 kt wiz 파크", "수원시 장안구", "127.0096685", "37.2997553");
    public static final Stadium SAJIK = new Stadium(6L, "사직야구장", "부산광역시 동래구", "129.0615183", "35.1940316");
    public static final Stadium DAEGU = new Stadium(7L, "대구삼성라이온즈파크", "대구광역시 수성구", "128.6815273", "35.8411705");
    public static final Stadium GOCHEOK = new Stadium(8L, "고척스카이돔", "서울특별시 구로구", "126.8670866", "37.498931");
    public static final Stadium DAEJEON = new Stadium(9L, "한화생명이글스파크", "대전광역시 중구", "127.4291345", "36.3170789");
    public static final Stadium ULSAN = new Stadium(10L, "울산 문수야구장", "울산광역시 남구", "129.249577", "35.535295");
    public static final Stadium ICHON_DOOSAN_BEARS_PARK = new Stadium(11L, "베어스 파크", "경기도 이천시", "127.497240", "37.243605");
    public static final Stadium CHEONGJU = new Stadium(12L, "청주야구장", "충청북도 청주시", "127.488345", "36.626893");



    public static final List<Stadium> STADIUMS = List.of(
            GWANGJU, JAMSIL, CHANGWON, INCHEON, SUWON, SAJIK, DAEGU, GOCHEOK, DAEJEON, ULSAN, ICHON_DOOSAN_BEARS_PARK, CHEONGJU
    );

    public static Stadium getById(Long id) {
        return STADIUMS.stream()
                .filter(stadium -> stadium.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STADIUM_NOT_FOUND_BY_ID));
    }

    public static Stadium getByName(String name) {
        return STADIUMS.stream()
                .filter(stadium -> stadium.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STADIUM_NOT_FOUND_BY_NAME));
    }
}
