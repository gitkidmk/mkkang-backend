package kr.mkkang.mkkangbackend.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    ANYONE("ROLE_ANY","일반사용자"),
    FAMILY("ROLE_FAMILY","가족사용자"),
    ADMIN("ROLE_ADMIN","관리자");

    private final String key;
    private final String title;
}
