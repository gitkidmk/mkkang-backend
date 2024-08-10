package kr.mkkang.mkkangbackend.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String profile;

    private String registrationId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Builder
    public Member(String name, String email, String profile, String registrationId, MemberRole role) {
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.registrationId = registrationId;
        this.role = role;
    }

    public Member update(String name, String profile) {
        this.name = name;
        this.profile = profile;
        return this;
    }
}