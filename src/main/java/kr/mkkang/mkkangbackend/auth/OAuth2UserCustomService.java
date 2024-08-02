package kr.mkkang.mkkangbackend.auth;

import kr.mkkang.mkkangbackend.domain.Member;
import kr.mkkang.mkkangbackend.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Assert.notNull(userRequest, "userRequest cannot be null");

        // 1. 유저 정보(attributes) 가져오기
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        // 2. resistrationId 가져오기 (third-party id)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. userNameAttributeName 가져오기
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 유저 정보 dto 생성
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

        // 5. 회원가입 및 로그인
        Member member = getOrSave(oAuth2UserInfo);

        // access && refresh token 생성 후 저장
        // 사용자 response에 넣어주기

        // OAuth2User로 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())),
                oAuth2UserAttributes,
                userNameAttributeName);
    }

    private Member getOrSave(OAuth2UserInfo oAuth2UserInfo) {
        return memberRepository.findByEmail(oAuth2UserInfo.email())
                .orElseGet(oAuth2UserInfo::toEntity);
    }
}
