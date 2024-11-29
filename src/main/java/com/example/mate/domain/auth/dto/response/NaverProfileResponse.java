package com.example.mate.domain.auth.dto.response;

import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NaverProfileResponse {

    private String name;
    //    private String nickname;  // 사용자 입력 nickname과 겹쳐 삭제 필요
    private String email;
    private String gender;
    private String birthyear;

    // JSON 응답 문자열을 파싱하여 NaverProfile 객체 생성
    public static NaverProfileResponse fromJson(String jsonResponseBody) {
        JsonElement element = JsonParser.parseString(jsonResponseBody);

        return NaverProfileResponse.builder()
                .name(element.getAsJsonObject().get("response").getAsJsonObject().get("name").getAsString())
//                .nickname(element.getAsJsonObject().get("response").getAsJsonObject().get("nickname").getAsString())
                .email(element.getAsJsonObject().get("response").getAsJsonObject().get("email").getAsString())
                .gender(element.getAsJsonObject().get("response").getAsJsonObject().get("gender").getAsString())
                .birthyear(element.getAsJsonObject().get("response").getAsJsonObject().get("birthyear").getAsString())
                .build();
    }
}