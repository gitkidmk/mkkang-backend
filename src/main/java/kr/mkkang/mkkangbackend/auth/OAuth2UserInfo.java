package kr.mkkang.mkkangbackend.auth;

import kr.mkkang.mkkangbackend.domain.Member;
import kr.mkkang.mkkangbackend.domain.MemberRole;
import lombok.Builder;

import java.util.Map;

/*
*  userInfo를 google/kakao에 맞춰 Member 엔티티에 저장할 수 있게 수정
*/

@Builder
public record OAuth2UserInfo(
        String name,
        String email,
        String profile
) {

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) { // registration id별로 userInfo 생성
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> ofGoogle(attributes);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profile((String) attributes.get("picture"))
                .build();
    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
                .name((String) profile.get("nickname"))
                .email((String) account.get("email"))
                .profile((String) profile.get("profile_image_url"))
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .profile(profile)
                .role(MemberRole.ANYONE)
                .build();
    }
}