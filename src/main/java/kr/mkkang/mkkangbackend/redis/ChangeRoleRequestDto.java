package kr.mkkang.mkkangbackend.redis;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ChangeRoleRequestDto {
    private String email;
    private String role;
}
